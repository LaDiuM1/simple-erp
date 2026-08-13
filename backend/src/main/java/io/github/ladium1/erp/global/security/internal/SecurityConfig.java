package io.github.ladium1.erp.global.security.internal;

import io.github.ladium1.erp.employee.api.LoginAccountApi;
import io.github.ladium1.erp.global.demo.DemoRequestGuardFilter;
import io.github.ladium1.erp.global.demo.DemoIngressGuardFilter;
import io.github.ladium1.erp.global.logging.LoggingMdcFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAccountApi loginAccountApi;
    private final HandlerExceptionResolver exceptionResolver;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            LoginAccountApi loginAccountApi,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAccountApi = loginAccountApi;
        this.exceptionResolver = exceptionResolver;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DemoIngressGuardFilter demoIngressGuardFilter,
            DemoRequestGuardFilter demoRequestGuardFilter
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 필터 예외를 글로벌 핸들러로 전달
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(request, response, null, authException))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(request, response, null, accessDeniedException))
                )

                // 전역 권한 설정 (세부 권한은 컨트롤러 위임)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/*/auth/**").permitAll()
                        .requestMatchers("/api/v1/demo/status").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 공개 API burst는 JWT 검증·계정 상태 DB 조회 전에 먼저 제한한다.
                .addFilterBefore(demoIngressGuardFilter, UsernamePasswordAuthenticationFilter.class)

                .addFilterAfter(
                        new JwtAuthenticationFilter(jwtTokenProvider, loginAccountApi),
                        DemoIngressGuardFilter.class)

                // 인증 직후 MDC를 먼저 부착해 demo guard의 조기 거부 응답도 같은 trace 계약을 유지
                .addFilterAfter(new LoggingMdcFilter(), JwtAuthenticationFilter.class)

                // JWT/MDC 해석 뒤 계정 단위 write limit 적용, servlet 자동 등록은 별도로 비활성화
                .addFilterAfter(demoRequestGuardFilter, LoggingMdcFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(LoggingMdcFilter.TRACE_ID_HEADER, "Retry-After"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
