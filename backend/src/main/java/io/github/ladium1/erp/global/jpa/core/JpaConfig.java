package io.github.ladium1.erp.global.jpa.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA 변경 감지 설정 =
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}