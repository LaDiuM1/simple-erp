package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.employee.api.LoginAccountApi;
import io.github.ladium1.erp.employee.internal.dto.TokenResponse;
import io.github.ladium1.erp.employee.internal.service.AuthService;
import io.github.ladium1.erp.employee.internal.web.AuthController;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.logging.LoggingMdcFilter;
import io.github.ladium1.erp.global.security.internal.JwtTokenProvider;
import io.github.ladium1.erp.global.security.internal.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        DemoStatusController.class,
        AuthController.class
})
@Import({
        SecurityConfig.class,
        DemoFilterConfiguration.class,
        DemoProperties.class,
        DemoGuardProbeController.class
})
@TestPropertySource(properties = {
        "demo.enabled=true",
        "demo.seed.expected-version=test-seed",
        "demo.upload.enabled=true",
        "demo.rate-limit.login-limit=10",
        "demo.rate-limit.login-global-limit=30",
        "demo.rate-limit.write-limit=60",
        "demo.rate-limit.write-global-limit=90",
        "demo.rate-limit.ingress-limit=300",
        "demo.rate-limit.ingress-global-limit=600",
        "demo.rate-limit.max-concurrent-ingress=8",
        "demo.rate-limit.max-concurrent-writes=4",
        "demo.rate-limit.read-limit=120",
        "demo.rate-limit.read-global-limit=180",
        "demo.rate-limit.preview-limit=20",
        "demo.rate-limit.preview-global-limit=30",
        "demo.rate-limit.max-concurrent-reads=4",
        "demo.rate-limit.max-concurrent-previews=2",
        "demo.rate-limit.upload-limit=10",
        "demo.rate-limit.upload-global-limit=16",
        "demo.rate-limit.excel-upload-limit=2",
        "demo.rate-limit.excel-upload-global-limit=2",
        "demo.rate-limit.download-limit=20",
        "demo.rate-limit.download-global-limit=30",
        "demo.rate-limit.download-byte-window=PT1H",
        "demo.rate-limit.window=PT1M",
        "cors.allowed-origins=http://localhost:5173"
})
class DemoSecurityFilterChainTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @Autowired private MockMvc mockMvc;
    @Autowired private DemoProperties properties;
    @Autowired private DemoRateLimiter rateLimiter;
    @Autowired private DemoRequestConcurrencyLimiter requestConcurrencyLimiter;
    @Autowired private FilterRegistrationBean<DemoRequestGuardFilter> demoRequestGuardFilterRegistration;
    @Autowired private FilterRegistrationBean<DemoIngressGuardFilter> demoIngressGuardFilterRegistration;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private LoginAccountApi loginAccountApi;
    @MockitoBean private DemoStartupVerificationGate startupVerificationGate;
    @MockitoBean private DemoProtectionPolicy protectionPolicy;
    @MockitoBean private DemoStateStore stateStore;
    @MockitoBean private AuthService authService;

    private final AtomicReference<String> authenticatedLoginId = new AtomicReference<>("demo.staff");

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
        properties.getRateLimit().setUploadLimit(10);
        properties.getRateLimit().setWriteLimit(60);
        properties.getRateLimit().setWriteGlobalLimit(90);
        properties.getRateLimit().setIngressLimit(300);
        properties.getRateLimit().setIngressGlobalLimit(600);
        properties.getRateLimit().setMaxConcurrentIngress(8);
        properties.getRateLimit().setMaxConcurrentWrites(4);
        properties.getRateLimit().setUploadGlobalLimit(16);
        properties.getRateLimit().setExcelUploadLimit(2);
        properties.getRateLimit().setExcelUploadGlobalLimit(2);
        properties.getRateLimit().setDownloadLimit(20);
        properties.getRateLimit().setDownloadGlobalLimit(30);
        properties.getRateLimit().setDownloadByteLimit(64L * 1024 * 1024);
        properties.getRateLimit().setDownloadGlobalByteLimit(96L * 1024 * 1024);
        properties.getRateLimit().setReadLimit(120);
        properties.getRateLimit().setReadGlobalLimit(180);
        properties.getRateLimit().setPreviewLimit(20);
        properties.getRateLimit().setPreviewGlobalLimit(30);
        properties.getRateLimit().setMaxConcurrentReads(4);
        properties.getRateLimit().setMaxConcurrentPreviews(2);
        rateLimiter.clear();
        authenticatedLoginId.set("demo.staff");
        given(jwtTokenProvider.validateToken("test-token")).willReturn(true);
        given(jwtTokenProvider.getAuthentication("test-token")).willAnswer(ignored ->
                new UsernamePasswordAuthenticationToken(
                        authenticatedLoginId.get(), "", List.of()));
        given(loginAccountApi.isLoginAllowed(any())).willReturn(true);
        given(authService.login(any())).willReturn(new TokenResponse("issued-token"));
        given(stateStore.current()).willReturn(new DemoStatusResponse(
                true, "DEMO", DemoState.READY,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"), "generation-1", null,
                null, null, 300, 120, false, "합성 데이터", true, null, List.of()));
    }

    @Test
    @DisplayName("status는 익명 접근 가능하고 기존 envelope를 유지")
    void anonymous_status_is_public() throws Exception {
        mockMvc.perform(get("/api/v1/demo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("runner 완료를 나타내는 readiness probe는 인증 없이 조회 가능")
    void anonymous_readiness_probe_is_public() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("ready"));
    }

    @Test
    @DisplayName("demo off에서는 guard가 쓰기 요청을 그대로 통과")
    void demo_off_passes_through() throws Exception {
        properties.setEnabled(false);
        authenticatedLoginId.set("off-account");

        mockMvc.perform(post("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());

        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("로그인은 IP당 10회 허용하고 11번째에 429 안정 코드와 Retry-After")
    void eleventh_login_is_rate_limited() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.10");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("서로 다른 IP를 합친 로그인 30회는 허용하고 31번째는 global 429")
    void thirty_first_distributed_login_is_globally_rate_limited() throws Exception {
        for (int i = 0; i < 30; i++) {
            String remoteAddress = "198.51.100." + (i + 1);
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.250");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("percent-encoded 로그인 경로도 같은 handler rate limit을 공유")
    void encoded_login_paths_cannot_bypass_rate_limit() throws Exception {
        List<String> paths = List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/%6cogin",
                "/api/v1/auth/%6Cogin",
                "/api/v1/auth/l%6fgin",
                "/api/v1/auth/log%69n",
                "/api/v1/auth/%6C%6F%67%69%6E",
                "/api/v1/auth/%6cogin",
                "/api/v1/auth/l%6fgin",
                "/api/v1/auth/log%69n",
                "/api/v1/auth/%6C%6F%67%69%6E"
        );

        for (String path : paths) {
            mockMvc.perform(post(URI.create(path))
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.11");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post(URI.create("/api/v1/auth/%6cogin"))
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.11");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("로그인 limit은 요청 본문 역직렬화 전에 적용")
    void malformed_login_payloads_are_rate_limited_before_body_parsing() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.12");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{"))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.12");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("startup 검증 전에는 조회를 보존하고 쓰기만 503으로 차단")
    void startup_verification_gate_blocks_only_writes() throws Exception {
        authenticatedLoginId.set("startup-account");
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(startupVerificationGate).assertWriteReady();

        mockMvc.perform(get("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DEMO_RESET_IN_PROGRESS"));

        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("write lock 중 POST는 503 + DEMO_RESET_IN_PROGRESS")
    void locked_write_returns_stable_503() throws Exception {
        authenticatedLoginId.set("locked-account");
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(protectionPolicy).assertWriteAvailable();

        mockMvc.perform(post("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().exists(LoggingMdcFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.code").value("DEMO_RESET_IN_PROGRESS"));
        assertThat(requestConcurrencyLimiter.activeIngressCount()).isZero();
        assertThat(requestConcurrencyLimiter.activeWriteCount()).isZero();
    }

    @Test
    @DisplayName("write lock 중에도 canonical·encoded 로그인은 같은 handler로 진입한다")
    void login_remains_available_during_write_lock() throws Exception {
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(protectionPolicy).assertWriteAvailable();

        for (String path : List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/%6cogin",
                "/api/v1/auth/log%69n")) {
            mockMvc.perform(post(URI.create(path))
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.13");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"demo.staff\",\"password\":\"public\"}"))
                    .andExpect(status().isOk());
        }

        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("인증 GET은 write guard 없이 조회 전용 보호를 거쳐 통과")
    void authenticated_get_passes() throws Exception {
        authenticatedLoginId.set("reader-account");

        mockMvc.perform(get("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());

        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("ingress 한도 초과는 JWT 계정 DB 조회 전에 거부")
    void ingress_limit_runs_before_jwt_account_lookup() throws Exception {
        properties.getRateLimit().setIngressLimit(1);
        properties.getRateLimit().setIngressGlobalLimit(10);

        mockMvc.perform(get("/api/v1/probe")
                        .with(request -> { request.setRemoteAddr("198.51.100.10"); return request; })
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe")
                        .with(request -> { request.setRemoteAddr("198.51.100.10"); return request; })
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());

        verify(loginAccountApi, times(1)).isLoginAllowed(any());
        assertThat(requestConcurrencyLimiter.activeIngressCount()).isZero();
    }

    @Test
    @DisplayName("서로 다른 IP도 ingress global 한도를 원자적으로 공유")
    void ingress_global_rate_is_atomic() throws Exception {
        properties.getRateLimit().setIngressLimit(2);
        properties.getRateLimit().setIngressGlobalLimit(3);

        for (String address : List.of("198.51.100.31", "198.51.100.31", "198.51.100.32")) {
            mockMvc.perform(get("/api/v1/probe")
                            .with(request -> { request.setRemoteAddr(address); return request; })
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/v1/probe")
                        .with(request -> { request.setRemoteAddr("198.51.100.32"); return request; })
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("쉼표·hostname XFF는 신뢰하지 않고 실제 peer 한도를 공유")
    void ingress_rejects_untrusted_forwarded_for_identity() throws Exception {
        properties.getRateLimit().setIngressLimit(1);
        properties.getRateLimit().setIngressGlobalLimit(10);

        for (String forwarded : List.of("203.0.113.1, 198.51.100.1", "attacker.example")) {
            mockMvc.perform(get("/api/v1/probe")
                            .with(request -> { request.setRemoteAddr("198.51.100.20"); return request; })
                            .header("X-Forwarded-For", forwarded)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(forwarded.contains(",") ? status().isOk() : status().isTooManyRequests());
        }
        verify(loginAccountApi, times(1)).isLoginAllowed(any());
    }

    @Test
    @DisplayName("encoded API prefix도 canonical 요청과 ingress bucket을 공유")
    void encoded_api_prefix_cannot_bypass_ingress_limit() throws Exception {
        properties.getRateLimit().setIngressLimit(1);
        properties.getRateLimit().setIngressGlobalLimit(10);

        mockMvc.perform(get(URI.create("/%61pi/v1/probe"))
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("OPTIONS와 공개 status GET/HEAD는 ingress budget을 소비하지 않음")
    void cheap_public_requests_do_not_consume_ingress_budget() throws Exception {
        properties.getRateLimit().setIngressLimit(1);
        properties.getRateLimit().setIngressGlobalLimit(10);

        mockMvc.perform(options("/api/v1/probe")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/demo/status")).andExpect(status().isOk());
        mockMvc.perform(head("/api/v1/demo/status")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ingress global concurrency가 차면 JWT 전에 429")
    void ingress_concurrency_is_bounded_before_jwt() throws Exception {
        properties.getRateLimit().setMaxConcurrentIngress(2);
        DemoRequestConcurrencyLimiter.Lease first = requestConcurrencyLimiter.tryAcquireIngress();
        DemoRequestConcurrencyLimiter.Lease second = requestConcurrencyLimiter.tryAcquireIngress();
        try {
            mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().string("Retry-After", "1"));
            verify(loginAccountApi, never()).isLoginAllowed(any());
        } finally {
            first.close();
            second.close();
        }
    }

    @Test
    @DisplayName("GET과 HEAD는 같은 계정 read 한도를 공유")
    void authenticated_get_and_head_share_read_rate_limit() throws Exception {
        properties.getRateLimit().setReadLimit(1);
        properties.getRateLimit().setReadGlobalLimit(10);
        authenticatedLoginId.set("bounded-reader");

        mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(head("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("encoded API prefix GET도 canonical 요청과 read bucket을 공유")
    void encoded_api_prefix_cannot_bypass_read_limit() throws Exception {
        properties.getRateLimit().setReadLimit(1);
        properties.getRateLimit().setReadGlobalLimit(10);

        mockMvc.perform(get(URI.create("/%61pi/v1/probe"))
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("서로 다른 계정의 read도 global 한도를 원자적으로 공유")
    void authenticated_reads_share_atomic_global_rate_limit() throws Exception {
        properties.getRateLimit().setReadLimit(10);
        properties.getRateLimit().setReadGlobalLimit(2);

        for (String identity : List.of("reader-a", "reader-b")) {
            authenticatedLoginId.set(identity);
            mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }
        authenticatedLoginId.set("reader-c");
        mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("인증 read global concurrency가 차면 handler 진입 전에 429")
    void authenticated_read_concurrency_is_bounded() throws Exception {
        properties.getRateLimit().setMaxConcurrentReads(2);
        DemoRequestConcurrencyLimiter.Lease first = requestConcurrencyLimiter.tryAcquireRead();
        DemoRequestConcurrencyLimiter.Lease second = requestConcurrencyLimiter.tryAcquireRead();
        try {
            mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().string("Retry-After", "1"));
        } finally {
            first.close();
            second.close();
        }
    }

    @Test
    @DisplayName("기본 120/min read 한도는 UI 초기 조회 burst를 보존")
    void ui_initial_read_burst_is_preserved() throws Exception {
        authenticatedLoginId.set("ui-burst-reader");
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("write lock 중에도 상태를 바꾸지 않는 코드 미리보기 POST는 통과")
    void code_rule_preview_passes_during_write_lock() throws Exception {
        authenticatedLoginId.set("preview-account");
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(protectionPolicy).assertWriteAvailable();

        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("preview:CUSTOMER"));

        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("preview는 account/global paired 한도를 적용")
    void code_rule_preview_is_rate_limited() throws Exception {
        properties.getRateLimit().setPreviewLimit(1);
        properties.getRateLimit().setPreviewGlobalLimit(10);
        authenticatedLoginId.set("preview-rate-account");

        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("encoded preview 경로도 canonical preview bucket과 reset 허용을 공유")
    void encoded_code_rule_preview_uses_same_guard() throws Exception {
        properties.getRateLimit().setPreviewLimit(1);
        properties.getRateLimit().setPreviewGlobalLimit(10);
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(protectionPolicy).assertWriteAvailable();

        mockMvc.perform(post(URI.create("/api/v1/code-rules/CUSTOMER/pre%76iew"))
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
        verify(protectionPolicy, never()).assertWriteAvailable();
    }

    @Test
    @DisplayName("preview global 한도는 서로 다른 계정도 원자적으로 공유")
    void code_rule_preview_global_rate_is_atomic() throws Exception {
        properties.getRateLimit().setPreviewLimit(2);
        properties.getRateLimit().setPreviewGlobalLimit(3);

        for (String identity : List.of("preview-a", "preview-a", "preview-b")) {
            authenticatedLoginId.set(identity);
            mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }
        authenticatedLoginId.set("preview-b");
        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("preview global concurrency가 차면 handler 진입 전에 429")
    void code_rule_preview_concurrency_is_bounded() throws Exception {
        properties.getRateLimit().setMaxConcurrentPreviews(1);
        DemoRequestConcurrencyLimiter.Lease lease = requestConcurrencyLimiter.tryAcquirePreview();
        try {
            mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().string("Retry-After", "1"));
        } finally {
            lease.close();
        }
    }

    @Test
    @DisplayName("4개 업로드 경로는 허용 상태에서 실제 handler까지 전달")
    void all_upload_paths_are_allowed() throws Exception {
        authenticatedLoginId.set("upload-account");
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.txt", "text/plain", "payload".getBytes());

        for (String path : List.of(
                "/api/v1/files",
                "/api/v1/drive/files",
                "/api/v1/customers/excel/upload",
                "/api/v1/sales-contacts/excel/upload")) {
            mockMvc.perform(multipart(path)
                            .file(file)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.result").value("upload"));
        }

        verify(protectionPolicy, times(4)).assertWriteAvailable();
        verify(protectionPolicy, times(4)).assertUploadAllowed();
    }

    @Test
    @DisplayName("허용된 4개 POST 외 multipart는 모든 early-return보다 먼저 415")
    void multipart_cannot_bypass_upload_guard_through_public_or_preview_paths() throws Exception {
        mockMvc.perform(get("/api/v1/demo/status")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("DEMO_UNSUPPORTED_MULTIPART"));

        mockMvc.perform(post("/api/v1/demo/status")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("DEMO_UNSUPPORTED_MULTIPART"));

        mockMvc.perform(post("/actuator/health")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("DEMO_UNSUPPORTED_MULTIPART"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("DEMO_UNSUPPORTED_MULTIPART"));

        mockMvc.perform(post("/api/v1/code-rules/CUSTOMER/preview")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("DEMO_UNSUPPORTED_MULTIPART"));
    }

    @Test
    @DisplayName("허용된 upload 경로도 인증이 없으면 multipart body parse 전에 401")
    void anonymous_allowed_upload_is_rejected_by_guard() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.txt", "text/plain", "payload".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));

        verify(protectionPolicy, never()).assertUploadAllowed();
    }

    @Test
    @DisplayName("reset lock 중에는 업로드 본문 파싱 전에 503")
    void upload_is_blocked_by_reset_lock() throws Exception {
        authenticatedLoginId.set("locked-upload-account");
        doThrow(new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS))
                .when(protectionPolicy).assertWriteAvailable();
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.txt", "text/plain", "payload".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DEMO_RESET_IN_PROGRESS"));

        verify(protectionPolicy, never()).assertUploadAllowed();
    }

    @Test
    @DisplayName("동일 계정 upload 10회는 허용하고 11번째는 body 처리 전에 429")
    void eleventh_account_upload_is_rate_limited() throws Exception {
        for (int i = 0; i < 10; i++) {
            performUpload("upload-rate-account").andExpect(status().isOk());
        }

        performUpload("upload-rate-account")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("두 공개 계정을 합친 upload 16회는 허용하고 17번째는 global 429")
    void seventeenth_global_upload_is_rate_limited() throws Exception {
        for (int i = 0; i < 8; i++) {
            performUpload("upload-global-a").andExpect(status().isOk());
            performUpload("upload-global-b").andExpect(status().isOk());
        }

        performUpload("upload-global-a")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Excel import는 계정과 global 모두 분당 2회까지만 허용")
    void third_excel_import_is_rate_limited() throws Exception {
        performExcelUpload("excel-rate-account", "/api/v1/customers/excel/upload")
                .andExpect(status().isOk());
        performExcelUpload("excel-rate-account", "/api/v1/sales-contacts/excel/upload")
                .andExpect(status().isOk());

        performExcelUpload("excel-rate-account", "/api/v1/customers/excel/upload")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("download 20회는 허용하고 21번째는 전송 전에 account 429")
    void twenty_first_account_download_is_rate_limited() throws Exception {
        for (int i = 0; i < 20; i++) {
            performDownload("download-rate-account", i + 1).andExpect(status().isOk());
        }

        performDownload("download-rate-account", 21)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("두 공개 계정을 합친 download 30회 이후 global 429")
    void thirty_first_global_download_is_rate_limited() throws Exception {
        for (int i = 0; i < 15; i++) {
            performDownload("download-global-a", i + 1).andExpect(status().isOk());
            performDownload("download-global-b", i + 101).andExpect(status().isOk());
        }

        performDownload("download-global-a", 999)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("자동 HEAD download도 GET과 동일한 account count 보호를 적용")
    void twenty_first_head_download_is_rate_limited() throws Exception {
        for (int i = 0; i < 20; i++) {
            performHeadDownload("head-rate-account", i + 1).andExpect(status().isOk());
        }

        performHeadDownload("head-rate-account", 21)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("실제 ByteArrayResource 크기는 계정·global 1시간 budget을 응답 전에 소비")
    void download_bytes_are_weighted_before_response_write() throws Exception {
        properties.getRateLimit().setDownloadByteLimit(7);
        properties.getRateLimit().setDownloadGlobalByteLimit(14);

        performDownload("download-byte-account", 1).andExpect(status().isOk());
        performDownload("download-byte-account", 2)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "3600"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("동일 계정 쓰기는 60회 허용하고 61번째에 429")
    void sixty_first_account_write_is_rate_limited() throws Exception {
        authenticatedLoginId.set("rate-account");
        for (int i = 0; i < 60; i++) {
            mockMvc.perform(post("/api/v1/probe")
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMO_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("일반 write는 서로 다른 계정도 global 한도를 공유")
    void writes_share_global_rate_limit() throws Exception {
        properties.getRateLimit().setWriteLimit(10);
        properties.getRateLimit().setWriteGlobalLimit(2);

        for (String identity : List.of("writer-a", "writer-b")) {
            authenticatedLoginId.set(identity);
            mockMvc.perform(post("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk());
        }
        authenticatedLoginId.set("writer-c");
        mockMvc.perform(post("/api/v1/probe").header("Authorization", AUTHORIZATION))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("일반 write global concurrency가 차면 handler 전에 429")
    void write_concurrency_is_bounded() throws Exception {
        properties.getRateLimit().setMaxConcurrentWrites(2);
        DemoRequestConcurrencyLimiter.Lease first = requestConcurrencyLimiter.tryAcquireWrite();
        DemoRequestConcurrencyLimiter.Lease second = requestConcurrencyLimiter.tryAcquireWrite();
        try {
            mockMvc.perform(post("/api/v1/probe").header("Authorization", AUTHORIZATION))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().string("Retry-After", "1"));
        } finally {
            first.close();
            second.close();
        }
    }

    @Test
    @DisplayName("guard servlet 자동 등록은 꺼져 SecurityFilterChain에서 한 번만 실행")
    void servlet_registration_is_disabled() {
        assertThat(demoRequestGuardFilterRegistration.isEnabled()).isFalse();
        assertThat(demoIngressGuardFilterRegistration.isEnabled()).isFalse();
    }

    private org.springframework.test.web.servlet.ResultActions performUpload(String loginId) throws Exception {
        authenticatedLoginId.set(loginId);
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.txt", "text/plain", "payload".getBytes());
        return mockMvc.perform(multipart("/api/v1/files")
                .file(file)
                .header("Authorization", AUTHORIZATION));
    }

    private org.springframework.test.web.servlet.ResultActions performDownload(String loginId, long id)
            throws Exception {
        authenticatedLoginId.set(loginId);
        return mockMvc.perform(get("/api/v1/drive/files/{id}/download", id)
                .header("Authorization", AUTHORIZATION));
    }

    private org.springframework.test.web.servlet.ResultActions performExcelUpload(String loginId, String path)
            throws Exception {
        authenticatedLoginId.set(loginId);
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "payload".getBytes());
        return mockMvc.perform(multipart(path)
                .file(file)
                .header("Authorization", AUTHORIZATION));
    }

    private org.springframework.test.web.servlet.ResultActions performHeadDownload(String loginId, long id)
            throws Exception {
        authenticatedLoginId.set(loginId);
        return mockMvc.perform(head("/api/v1/drive/files/{id}/download", id)
                .header("Authorization", AUTHORIZATION));
    }
}
