package io.github.ladium1.erp.approval.internal.web;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentDownload;
import io.github.ladium1.erp.approval.internal.dto.ApprovalBox;
import io.github.ladium1.erp.approval.internal.dto.ApprovalCreateRequest;
import io.github.ladium1.erp.approval.internal.dto.ApprovalDetailResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSearchCondition;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSummaryResponse;
import io.github.ladium1.erp.approval.internal.dto.DecisionRequest;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import io.github.ladium1.erp.approval.internal.service.ApprovalService;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private static final String MENU_CODE = "APPROVALS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final ApprovalService approvalService;

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@AuthenticationPrincipal User user,
                       @Valid @RequestBody ApprovalCreateRequest request) {
        return approvalService.createGeneral(user.getUsername(), request);
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<ApprovalSummaryResponse> search(
            @AuthenticationPrincipal User user,
            @RequestParam ApprovalBox box,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) ApprovalDocType docType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return approvalService.search(
                user.getUsername(),
                new ApprovalSearchCondition(box, status, docType, keyword),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public ApprovalDetailResponse getDetail(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return approvalService.getDetail(user.getUsername(), id);
    }

    @GetMapping("/{id}/attachments/{fileId}")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadAttachment(@AuthenticationPrincipal User user,
                                                                @PathVariable Long id,
                                                                @PathVariable Long fileId) {
        ApprovalAttachmentDownload download = approvalService.downloadAttachment(user.getUsername(), id, fileId);
        return DownloadResponse.attachment(download.content(), download.name(), download.contentType());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(CAN_WRITE)
    public void approve(@AuthenticationPrincipal User user,
                        @PathVariable Long id,
                        @RequestBody(required = false) DecisionRequest request) {
        approvalService.approve(user.getUsername(), id, decisionOrEmpty(request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize(CAN_WRITE)
    public void reject(@AuthenticationPrincipal User user,
                       @PathVariable Long id,
                       @RequestBody(required = false) DecisionRequest request) {
        approvalService.reject(user.getUsername(), id, decisionOrEmpty(request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize(CAN_WRITE)
    public void cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        approvalService.cancel(user.getUsername(), id);
    }

    private static DecisionRequest decisionOrEmpty(DecisionRequest request) {
        return request != null ? request : new DecisionRequest(null);
    }
}
