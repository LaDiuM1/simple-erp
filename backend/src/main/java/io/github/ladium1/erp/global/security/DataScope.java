package io.github.ladium1.erp.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 행 단위 데이터 가시 범위.
 * <p>
 * 메뉴 권한 (canRead / canWrite) 과 직교하는 차원 — 사용자가 메뉴 진입 권한이 있더라도
 * 어떤 행을 볼 수 있는지를 별도로 제한.
 *
 * <p>도메인이 SELF / DEPARTMENT(_TREE) 를 적용하려면 자기 모듈에서 가시성 contributor 빈을
 * 등록한다. contributor 가 없는 메뉴에 ALL 외 스코프가 걸리면 빈 결과가 반환된다 (안전 우선).
 */
@Getter
@RequiredArgsConstructor
public enum DataScope {

    ALL("전체"),
    DEPARTMENT("본인 부서"),
    DEPARTMENT_TREE("본인 부서 + 하위"),
    SELF("본인");

    private final String label;

    /**
     * 다른 스코프보다 더 넓은 가시성이면 true.
     * <p>
     * 한 사용자가 같은 행을 여러 메뉴를 통해 볼 수 있을 때 (예: CUSTOMERS / SALES_CUSTOMERS) 더 permissive 한
     * 스코프를 채택한다 — 권한이 더 많은 사용자가 더 적게 보이는 일이 없도록.
     */
    public boolean isMorePermissiveThan(DataScope other) {
        return permissiveness() > other.permissiveness();
    }

    private int permissiveness() {
        return switch (this) {
            case ALL -> 3;
            case DEPARTMENT_TREE -> 2;
            case DEPARTMENT -> 1;
            case SELF -> 0;
        };
    }
}
