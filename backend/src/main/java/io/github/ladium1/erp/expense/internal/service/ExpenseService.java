package io.github.ladium1.erp.expense.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.expense.internal.dto.ExpenseCreateRequest;
import io.github.ladium1.erp.expense.internal.dto.ExpenseDetailResponse;
import io.github.ladium1.erp.expense.internal.dto.ExpenseReceiptDownload;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchCondition;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchScope;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSummaryResponse;
import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import io.github.ladium1.erp.expense.internal.entity.ExpenseItem;
import io.github.ladium1.erp.expense.internal.exception.ExpenseErrorCode;
import io.github.ladium1.erp.expense.internal.repository.ExpenseClaimRepository;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private static final BigDecimal MAX_TOTAL_AMOUNT = new BigDecimal("9999999999999.99");

    private final ExpenseClaimRepository expenseClaimRepository;
    private final ApprovalApi approvalApi;
    private final EmployeeApi employeeApi;
    private final FileStorageApi fileStorageApi;
    private final MenuPermissionEvaluator menuPermissionEvaluator;
    private final DemoProtectionPolicy demoProtectionPolicy;

    /**
     * 경비 청구 생성 — 총액을 서버에서 합산하고, 저장 즉시 전자결재에 상신해 문서 ID 를 연결한다.
     */
    @Auditable(menu = Menu.EXPENSES, action = AuditAction.CREATE, targetType = "ExpenseClaim", targetIdFromReturn = true)
    @Transactional
    public Long create(String loginId, ExpenseCreateRequest request) {
        Long claimantId = currentEmployee(loginId).id();

        List<ExpenseCreateRequest.ItemRequest> items = request.items();
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ExpenseErrorCode.EMPTY_ITEMS);
        }
        demoProtectionPolicy.assertNoAttachmentIds(collectReceiptFileIds(items));

        BigDecimal totalAmount = items.stream()
                .map(ExpenseCreateRequest.ItemRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAmount.compareTo(MAX_TOTAL_AMOUNT) > 0) {
            throw new BusinessException(ExpenseErrorCode.TOTAL_AMOUNT_EXCEEDED);
        }

        ExpenseClaim claim = ExpenseClaim.builder()
                .claimantId(claimantId)
                .title(request.title())
                .totalAmount(totalAmount)
                .build();
        items.forEach(item -> claim.addItem(
                item.expenseDate(), item.category(), item.amount(), item.description(), item.receiptFileId()));
        expenseClaimRepository.save(claim);

        List<Long> receiptFileIds = collectReceiptFileIds(items);
        FileOwner owner = FileOwner.expenseClaim(claim.getId());
        fileStorageApi.claim(receiptFileIds, owner, claimantId);

        Long approvalDocumentId = approvalApi.submit(ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.EXPENSE)
                .title(request.title())
                .content(summarizeItems(items, totalAmount))
                .drafterId(claimantId)
                .refId(claim.getId())
                .approverIds(request.approverIds())
                .attachmentFileIds(receiptFileIds)
                .build());
        claim.linkApprovalDocument(approvalDocumentId);

        return claim.getId();
    }

    /**
     * 경비 청구 목록 — MINE 은 본인 청구만, ALL 은 정산 관리자 (EXPENSES write 권한) 전용 전체 조회.
     */
    public PageResponse<ExpenseSummaryResponse> search(String loginId, ExpenseSearchScope scope,
                                                       ExpenseSearchCondition condition, Pageable pageable) {
        Long claimantId;
        if (scope == ExpenseSearchScope.ALL) {
            if (!canManageSettlement()) {
                throw new AccessDeniedException("경비 청구 전체 조회는 정산 관리자만 가능합니다.");
            }
            claimantId = null;
        } else {
            claimantId = currentEmployee(loginId).id();
        }
        Page<ExpenseClaim> page = expenseClaimRepository.search(claimantId, condition, pageable);
        Map<Long, String> nameById = claimantNames(
                page.getContent().stream().map(ExpenseClaim::getClaimantId).distinct().toList()
        );
        return PageResponse.of(page.map(claim -> toSummaryResponse(claim, nameById.get(claim.getClaimantId()))));
    }

    /**
     * 경비 청구 상세 — 청구자 본인 또는 정산 관리자만 접근하고, 그 외에는 존재를 숨긴다.
     */
    public ExpenseDetailResponse getDetail(String loginId, Long id) {
        ExpenseClaim claim = getAccessibleClaim(loginId, id);
        return toDetailResponse(claim, employeeApi.getById(claim.getClaimantId()).name());
    }

    /**
     * 영수증 다운로드 — 상세와 동일한 접근 규칙 + 해당 청구 항목에 연결된 파일인지 확인 (아니면 CLAIM_NOT_FOUND 은닉).
     */
    public ExpenseReceiptDownload downloadReceipt(String loginId, Long id, Long fileId) {
        ExpenseClaim claim = getAccessibleClaim(loginId, id);
        boolean attached = claim.getItems().stream()
                .anyMatch(item -> fileId.equals(item.getReceiptFileId()));
        if (!attached) {
            throw new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND);
        }
        FileOwner owner = FileOwner.expenseClaim(id);
        StoredFileInfo info = fileStorageApi.getInfo(fileId, owner);
        return new ExpenseReceiptDownload(info.originalName(), info.contentType(),
                fileStorageApi.loadContent(fileId, owner));
    }

    private ExpenseClaim getAccessibleClaim(String loginId, Long id) {
        Long employeeId = currentEmployee(loginId).id();
        ExpenseClaim claim = expenseClaimRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND));
        boolean accessible = claim.getClaimantId().equals(employeeId)
                || canManageSettlement();
        if (!accessible) {
            throw new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND);
        }
        return claim;
    }

    /**
     * 정산 관리자 여부 — EXPENSES 메뉴 write 권한으로 판별 (BoardService 의 NOTICE 검사와 동일 패턴).
     */
    private boolean canManageSettlement() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return menuPermissionEvaluator.canWrite(authentication, Menu.EXPENSES.name());
    }

    private EmployeeInfo currentEmployee(String loginId) {
        return employeeApi.findByLoginId(loginId)
                .orElseThrow(() -> new AccessDeniedException("인증된 직원 정보를 찾을 수 없습니다."));
    }

    /**
     * 결재 문서 본문용 항목 내역 — 결재자가 문서만으로 검토할 수 있게 전 항목을 줄 단위로 나열하고 합계로 마감.
     */
    private String summarizeItems(List<ExpenseCreateRequest.ItemRequest> items, BigDecimal totalAmount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        String lines = items.stream()
                .map(item -> {
                    String line = item.expenseDate() + " " + item.category().getLabel()
                            + " " + numberFormat.format(item.amount()) + "원";
                    return StringUtils.hasText(item.description()) ? line + " " + item.description() : line;
                })
                .collect(Collectors.joining("\n"));
        return lines + "\n합계 " + numberFormat.format(totalAmount) + "원";
    }

    private List<Long> collectReceiptFileIds(List<ExpenseCreateRequest.ItemRequest> items) {
        return items.stream()
                .map(ExpenseCreateRequest.ItemRequest::receiptFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Map<Long, String> claimantNames(List<Long> employeeIds) {
        return employeeApi.findByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeInfo::id, EmployeeInfo::name));
    }

    private ExpenseSummaryResponse toSummaryResponse(ExpenseClaim claim, String claimantName) {
        return ExpenseSummaryResponse.builder()
                .id(claim.getId())
                .title(claim.getTitle())
                .totalAmount(claim.getTotalAmount())
                .status(claim.getStatus())
                .claimantName(claimantName)
                .approvalDocumentId(claim.getApprovalDocumentId())
                .createdAt(claim.getCreatedAt())
                .itemCount(claim.getItems().size())
                .build();
    }

    private ExpenseDetailResponse toDetailResponse(ExpenseClaim claim, String claimantName) {
        List<Long> receiptFileIds = claim.getItems().stream()
                .map(ExpenseItem::getReceiptFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> receiptNameById = receiptFileIds.isEmpty()
                ? Map.of()
                : fileStorageApi.getInfos(receiptFileIds, FileOwner.expenseClaim(claim.getId())).stream()
                        .collect(Collectors.toMap(StoredFileInfo::id, StoredFileInfo::originalName, (a, b) -> a));

        List<ExpenseDetailResponse.ItemResponse> items = claim.getItems().stream()
                .map(item -> ExpenseDetailResponse.ItemResponse.builder()
                        .id(item.getId())
                        .expenseDate(item.getExpenseDate())
                        .category(item.getCategory())
                        .amount(item.getAmount())
                        .description(item.getDescription())
                        .receiptFileId(item.getReceiptFileId())
                        .receiptFileName(item.getReceiptFileId() == null
                                ? null
                                : receiptNameById.get(item.getReceiptFileId()))
                        .build())
                .toList();

        return ExpenseDetailResponse.builder()
                .id(claim.getId())
                .title(claim.getTitle())
                .totalAmount(claim.getTotalAmount())
                .status(claim.getStatus())
                .claimantName(claimantName)
                .approvalDocumentId(claim.getApprovalDocumentId())
                .createdAt(claim.getCreatedAt())
                .items(items)
                .build();
    }
}
