package io.github.ladium1.erp.member.internal.web;

import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.member.internal.dto.MemberCreateRequest;
import io.github.ladium1.erp.member.internal.dto.MemberDetailResponse;
import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.dto.MemberSearchCondition;
import io.github.ladium1.erp.member.internal.dto.MemberSummaryResponse;
import io.github.ladium1.erp.member.internal.dto.MemberUpdateRequest;
import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import io.github.ladium1.erp.member.internal.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private static final String MENU_CODE = "MDM_HRM";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final MemberService memberService;

    @GetMapping("/me")
    public MemberProfileResponse getMyInfo(@AuthenticationPrincipal User user) {
        return memberService.getMyInfo(user.getUsername());
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<MemberSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return memberService.search(toCondition(keyword, departmentId, positionId, roleId, status), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public MemberDetailResponse getDetail(@PathVariable Long id) {
        return memberService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) MemberStatus status,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = memberService.exportExcel(
                toCondition(keyword, departmentId, positionId, roleId, status),
                sort
        );
        String filename = "members_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody MemberCreateRequest request) {
        return memberService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody MemberUpdateRequest request) {
        memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }

    private MemberSearchCondition toCondition(String keyword, Long departmentId, Long positionId, Long roleId, MemberStatus status) {
        return new MemberSearchCondition(keyword, departmentId, positionId, roleId, status);
    }
}
