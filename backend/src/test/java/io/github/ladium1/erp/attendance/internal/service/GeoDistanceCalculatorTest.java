package io.github.ladium1.erp.attendance.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoDistanceCalculatorTest {

    // 사무실 = 서울시청 좌표, 허용 반경 300m
    private final GeoDistanceCalculator calculator = new GeoDistanceCalculator(37.5663, 126.9779, 300);

    @Test
    @DisplayName("같은 좌표는 거리 0")
    void distance_same_point_is_zero() {
        assertThat(calculator.distanceMeters(37.5663, 126.9779, 37.5663, 126.9779)).isZero();
    }

    @Test
    @DisplayName("위도 1도 차이는 약 111.2km")
    void distance_one_degree_latitude() {
        assertThat(calculator.distanceMeters(0, 0, 1, 0)).isCloseTo(111_195, within(100d));
    }

    @Test
    @DisplayName("반경 내 판정 — 약 222m 지점")
    void within_range_inside_boundary() {
        // 위도 +0.002도 ≈ 222m < 300m
        assertThat(calculator.isWithinOfficeRange(37.5683, 126.9779)).isTrue();
    }

    @Test
    @DisplayName("반경 밖 판정 — 약 445m 지점")
    void within_range_outside_boundary() {
        // 위도 +0.004도 ≈ 445m > 300m
        assertThat(calculator.isWithinOfficeRange(37.5703, 126.9779)).isFalse();
    }
}
