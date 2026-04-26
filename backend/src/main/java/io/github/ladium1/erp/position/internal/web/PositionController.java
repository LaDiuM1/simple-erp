package io.github.ladium1.erp.position.internal.web;

import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.position.internal.dto.PositionCreateRequest;
import io.github.ladium1.erp.position.internal.dto.PositionDetailResponse;
import io.github.ladium1.erp.position.internal.dto.PositionRankingRequest;
import io.github.ladium1.erp.position.internal.dto.PositionSearchCondition;
import io.github.ladium1.erp.position.internal.dto.PositionSummaryResponse;
import io.github.ladium1.erp.position.internal.dto.PositionUpdateRequest;
import io.github.ladium1.erp.position.internal.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private static final String MENU_CODE = "POSITIONS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /**
     * 드롭다운 / 필터로 소비되는 reference 목록 — 직원 관리 / 직책 관리 양쪽에서 사용.
     * 둘 중 하나라도 read 권한이 있으면 허용.
     */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, 'EMPLOYEES') "
            + "or @menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final PositionService positionService;

    @GetMapping
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<PositionInfo> findAll() {
        return positionService.findAll();
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_READ)
    public PageResponse<PositionSummaryResponse> search(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @PageableDefault(sort = "rankLevel", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return positionService.search(new PositionSearchCondition(codeKeyword, nameKeyword), pageable);
    }

    @GetMapping("/ranking")
    @PreAuthorize(CAN_READ)
    public List<PositionSummaryResponse> findAllByRanking() {
        return positionService.findAllByRanking();
    }

    @GetMapping("/code-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkCodeAvailability(@RequestParam String code) {
        return new AvailabilityResponse(positionService.isCodeAvailable(code));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public PositionDetailResponse getDetail(@PathVariable Long id) {
        return positionService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody PositionCreateRequest request) {
        return positionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody PositionUpdateRequest request) {
        positionService.update(id, request);
    }

    @PutMapping("/ranking")
    @PreAuthorize(CAN_WRITE)
    public void reorder(@Valid @RequestBody PositionRankingRequest request) {
        positionService.reorder(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        positionService.delete(id);
    }
}
