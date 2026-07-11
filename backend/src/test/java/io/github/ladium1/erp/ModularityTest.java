package io.github.ladium1.erp;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * 도메인 모듈 간 경계 자동 검증 (internal 침범 / 순환 의존).
 * <p>
 * global/* 은 모든 도메인이 참조하는 shared kernel 이면서 일부 서브패키지 (security 등) 가
 * employee / department 를 역참조하는 기존 순환이 있어 검증 대상에서 제외한다 —
 * 이 테스트는 도메인 모듈 간 경계만 잡는다.
 */
class ModularityTest {

    private static final ApplicationModules modules = ApplicationModules.of(
            ErpApplication.class,
            JavaClass.Predicates.resideInAPackage("io.github.ladium1.erp.global..")
    );

    @Test
    @DisplayName("도메인 모듈 경계 위반 없음")
    void verify_module_boundaries() {
        modules.verify();
    }
}
