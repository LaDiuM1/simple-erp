package io.github.ladium1.erp.attendance.internal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 사무실 좌표 기준 거리 / 반경 판정 유틸 — haversine 공식.
 */
@Component
public class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    private final double officeLatitude;
    private final double officeLongitude;
    private final double allowedRadiusMeters;

    public GeoDistanceCalculator(
            @Value("${erp.attendance.office.latitude}") double officeLatitude,
            @Value("${erp.attendance.office.longitude}") double officeLongitude,
            @Value("${erp.attendance.office.allowed-radius-meters}") double allowedRadiusMeters
    ) {
        this.officeLatitude = officeLatitude;
        this.officeLongitude = officeLongitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public boolean isWithinOfficeRange(double latitude, double longitude) {
        return distanceMeters(officeLatitude, officeLongitude, latitude, longitude) <= allowedRadiusMeters;
    }

    /** 두 좌표 사이 대원 거리 (m) — haversine 공식 */
    public double distanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
