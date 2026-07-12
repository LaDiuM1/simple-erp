package io.github.ladium1.erp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 컴포넌트 클래스의 단순명 (simple name) 유일성 검증.
 * <p>
 * Spring 은 기본 bean 이름을 단순 클래스명에서 만들기 때문에 서로 다른 모듈이 같은 이름의
 * 컴포넌트 (예: 두 모듈의 SupplierDeletionListener) 를 두면 ConflictingBeanDefinitionException 으로
 * 부팅이 죽는다 — 단위 / 슬라이스 테스트는 전체 컨텍스트를 띄우지 않아 이를 잡지 못하므로
 * 클래스패스 스캔으로 선제 차단한다. 중복 발생 시 소유 도메인 prefix 로 개명할 것
 * (예: ContractSupplierDeletionListener — Initializer 의 <Domain>Initializer 컨벤션과 동일 논리).
 */
class BeanNameUniquenessTest {

    @Test
    @DisplayName("컴포넌트 단순 클래스명 중복 없음 — bean 이름 충돌 부팅 실패 방지")
    void component_simple_names_are_unique() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        Map<String, List<String>> bySimpleName = scanner
                .findCandidateComponents("io.github.ladium1.erp").stream()
                .map(BeanDefinition::getBeanClassName)
                .filter(Objects::nonNull)
                .collect(groupingBy(className -> className.substring(className.lastIndexOf('.') + 1)));

        Map<String, List<String>> duplicated = bySimpleName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(duplicated)
                .as("단순 클래스명이 중복되면 Spring 기본 bean 이름이 충돌해 부팅이 실패한다 — 소유 도메인 prefix 로 개명할 것")
                .isEmpty();
    }
}
