package io.github.ladium1.erp.attendance.internal.repository;

import io.github.ladium1.erp.attendance.internal.dto.LeaveSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveRequestRepositoryCustom {

    /**
     * 관리자 휴가 신청 검색 — 기간 조건은 신청 기간과의 겹침 (overlap) 판정.
     */
    Page<LeaveRequest> search(LeaveSearchCondition condition, Pageable pageable);
}
