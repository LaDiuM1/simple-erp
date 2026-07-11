package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.ApprovalResultHandler;
import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.repository.LeaveRequestRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * LEAVE 문서 결재 결과 수신 — 승인 시 상태 전이 + 연차 차감, 반려 / 취소 시 상태만 전이.
 * LeaveService 를 의존하면 LeaveService -> ApprovalApi -> 핸들러 목록으로 이어지는 순환 참조가
 * 생기므로 리포지토리 / 잔여 provider 만 의존한다. (ExpenseApprovalResultHandler 와 동일 구조)
 */
@Component
@RequiredArgsConstructor
public class LeaveApprovalResultHandler implements ApprovalResultHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceProvider leaveBalanceProvider;

    @Override
    public ApprovalDocType docType() {
        return ApprovalDocType.LEAVE;
    }

    /**
     * 최종 승인 — 상태 전이 + 차감 대상 유형이면 이 시점에 잔여 차감.
     * 잔여 행을 잠그고 재검증한다 — 신청 이후 다른 승인으로 잔여가 줄었으면
     * 승인 트랜잭션 자체를 실패 (롤백) 시키는 것이 의미상 올바른 동작.
     */
    @Override
    @Transactional
    public void onApproved(Long refId) {
        LeaveRequest leaveRequest = getLeaveRequest(refId);
        leaveRequest.approve();
        if (leaveRequest.getLeaveType().isDeductible()) {
            LeaveBalance balance = leaveBalanceProvider.getOrCreateWithLock(
                    leaveRequest.getEmployeeId(), leaveRequest.getStartDate().getYear());
            if (balance.remainingDays().compareTo(leaveRequest.getDays()) < 0) {
                throw new BusinessException(AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE);
            }
            balance.use(leaveRequest.getDays());
        }
    }

    /** 반려 / 상신 취소 — 차감 없이 상태만 전이. */
    @Override
    @Transactional
    public void onRejected(Long refId) {
        getLeaveRequest(refId).reject();
    }

    private LeaveRequest getLeaveRequest(Long refId) {
        return leaveRequestRepository.findById(refId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.LEAVE_NOT_FOUND));
    }
}
