package io.github.ladium1.erp.attendance.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AttendanceErrorCode implements ErrorCode {

    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "오늘 이미 출근 처리되었습니다."),
    NOT_CHECKED_IN_YET(HttpStatus.CONFLICT, "출근 처리가 되어 있지 않습니다."),
    ALREADY_CHECKED_OUT(HttpStatus.CONFLICT, "오늘 이미 퇴근 처리되었습니다."),
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 근태 기록입니다."),
    INSUFFICIENT_LEAVE_BALANCE(HttpStatus.CONFLICT, "잔여 연차가 부족합니다."),
    INVALID_LEAVE_PERIOD(HttpStatus.BAD_REQUEST, "휴가 기간이 올바르지 않습니다."),
    DUPLICATE_LEAVE_PERIOD(HttpStatus.CONFLICT, "같은 기간에 이미 신청된 휴가가 있습니다."),
    LEAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 휴가 신청입니다.");

    private final HttpStatus status;
    private final String message;

}
