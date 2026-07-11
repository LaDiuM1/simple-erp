package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.attendance.internal.dto.AttendanceCorrectRequest;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.CheckInRequest;
import io.github.ladium1.erp.attendance.internal.dto.CheckOutRequest;
import io.github.ladium1.erp.attendance.internal.entity.Attendance;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.repository.AttendanceRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @InjectMocks
    private AttendanceService attendanceService;

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeApi employeeApi;
    @Mock private GeoDistanceCalculator geoDistanceCalculator;

    private static final String LOGIN_ID = "testUser";
    private static final Long EMPLOYEE_ID = 1L;
    private static final double LATITUDE = 37.5663;
    private static final double LONGITUDE = 126.9779;

    private final EmployeeInfo employee = EmployeeInfo.builder()
            .id(EMPLOYEE_ID)
            .loginId(LOGIN_ID)
            .name("테스트직원")
            .build();

    private Attendance checkedInAttendance() {
        return checkedInAttendance(LocalDate.now());
    }

    private Attendance checkedInAttendance(LocalDate workDate) {
        return Attendance.builder()
                .employeeId(EMPLOYEE_ID)
                .workDate(workDate)
                .checkInAt(workDate.atTime(9, 0))
                .checkInLatitude(LATITUDE)
                .checkInLongitude(LONGITUDE)
                .checkInWithinRange(true)
                .build();
    }

    @Test
    @DisplayName("체크인 성공 — 반경 내 판정")
    void check_in_success_within_range() {
        // given
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(geoDistanceCalculator.isWithinOfficeRange(LATITUDE, LONGITUDE)).willReturn(true);
        given(attendanceRepository.saveAndFlush(any(Attendance.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        AttendanceResponse response = attendanceService.checkIn(LOGIN_ID, new CheckInRequest(LATITUDE, LONGITUDE));

        // then
        assertThat(response.checkInAt()).isNotNull();
        assertThat(response.checkInWithinRange()).isTrue();
        assertThat(response.employeeName()).isEqualTo("테스트직원");
    }

    @Test
    @DisplayName("체크인 성공 — 반경 밖은 차단 없이 기록만")
    void check_in_success_out_of_range() {
        // given
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(geoDistanceCalculator.isWithinOfficeRange(LATITUDE, LONGITUDE)).willReturn(false);
        given(attendanceRepository.saveAndFlush(any(Attendance.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        AttendanceResponse response = attendanceService.checkIn(LOGIN_ID, new CheckInRequest(LATITUDE, LONGITUDE));

        // then
        assertThat(response.checkInAt()).isNotNull();
        assertThat(response.checkInWithinRange()).isFalse();
    }

    @Test
    @DisplayName("중복 체크인 시 409")
    void check_in_fail_already_checked_in() {
        // given
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.of(checkedInAttendance()));

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(LOGIN_ID, new CheckInRequest(LATITUDE, LONGITUDE)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.ALREADY_CHECKED_IN);
    }

    @Test
    @DisplayName("체크인 INSERT 경합 시 409 변환")
    void check_in_fail_insert_race() {
        // given — pre-check 는 통과했으나 동시 요청이 먼저 INSERT 해 유니크 제약 위반
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(geoDistanceCalculator.isWithinOfficeRange(LATITUDE, LONGITUDE)).willReturn(true);
        given(attendanceRepository.saveAndFlush(any(Attendance.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(LOGIN_ID, new CheckInRequest(LATITUDE, LONGITUDE)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.ALREADY_CHECKED_IN);
    }

    @Test
    @DisplayName("체크아웃 성공")
    void check_out_success() {
        // given
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.of(checkedInAttendance()));
        given(geoDistanceCalculator.isWithinOfficeRange(LATITUDE, LONGITUDE)).willReturn(true);

        // when
        AttendanceResponse response = attendanceService.checkOut(LOGIN_ID, new CheckOutRequest(LATITUDE, LONGITUDE));

        // then
        assertThat(response.checkOutAt()).isNotNull();
        assertThat(response.checkOutWithinRange()).isTrue();
    }

    @Test
    @DisplayName("자정 넘긴 퇴근 — 어제 미퇴근 행에 기록")
    void check_out_success_midnight_fallback() {
        // given — 오늘 행은 없고 어제 행이 미퇴근 상태
        LocalDate yesterday = LocalDate.now().minusDays(1);
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(attendanceRepository.findByEmployeeIdAndWorkDateAndCheckOutAtIsNull(eq(EMPLOYEE_ID), eq(yesterday)))
                .willReturn(Optional.of(checkedInAttendance(yesterday)));
        given(geoDistanceCalculator.isWithinOfficeRange(LATITUDE, LONGITUDE)).willReturn(true);

        // when
        AttendanceResponse response = attendanceService.checkOut(LOGIN_ID, new CheckOutRequest(LATITUDE, LONGITUDE));

        // then
        assertThat(response.workDate()).isEqualTo(yesterday);
        assertThat(response.checkOutAt()).isNotNull();
    }

    @Test
    @DisplayName("체크인 전 체크아웃 시 409")
    void check_out_fail_not_checked_in_yet() {
        // given — 오늘 행도, 어제 미퇴근 행도 없음
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(attendanceRepository.findByEmployeeIdAndWorkDateAndCheckOutAtIsNull(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.checkOut(LOGIN_ID, new CheckOutRequest(LATITUDE, LONGITUDE)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.NOT_CHECKED_IN_YET);
    }

    @Test
    @DisplayName("중복 체크아웃 시 409")
    void check_out_fail_already_checked_out() {
        // given
        Attendance attendance = checkedInAttendance();
        attendance.checkOut(LocalDateTime.now(), LATITUDE, LONGITUDE, true);
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
        given(attendanceRepository.findByEmployeeIdAndWorkDate(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .willReturn(Optional.of(attendance));

        // when & then
        assertThatThrownBy(() -> attendanceService.checkOut(LOGIN_ID, new CheckOutRequest(LATITUDE, LONGITUDE)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.ALREADY_CHECKED_OUT);
    }

    @Test
    @DisplayName("근태 정정 성공 — 시각만 보정, 위치 기록 유지")
    void correct_success() {
        // given
        Attendance attendance = checkedInAttendance();
        LocalDateTime correctedCheckIn = LocalDateTime.of(2026, 7, 6, 8, 30);
        LocalDateTime correctedCheckOut = LocalDateTime.of(2026, 7, 6, 18, 0);
        given(attendanceRepository.findById(10L)).willReturn(Optional.of(attendance));
        given(employeeApi.getById(EMPLOYEE_ID)).willReturn(employee);

        // when
        AttendanceResponse response = attendanceService.correct(
                10L, new AttendanceCorrectRequest(correctedCheckIn, correctedCheckOut));

        // then
        assertThat(response.checkInAt()).isEqualTo(correctedCheckIn);
        assertThat(response.checkOutAt()).isEqualTo(correctedCheckOut);
        assertThat(response.checkInWithinRange()).isTrue();
        assertThat(response.employeeName()).isEqualTo("테스트직원");
    }

    @Test
    @DisplayName("존재하지 않는 근태 정정 시 404")
    void correct_fail_not_found() {
        // given
        given(attendanceRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.correct(
                99L, new AttendanceCorrectRequest(LocalDateTime.now(), null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.ATTENDANCE_NOT_FOUND);
    }
}
