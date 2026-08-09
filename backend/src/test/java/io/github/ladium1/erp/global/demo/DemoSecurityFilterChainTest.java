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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        "demo.upload.enabled=true",
        "demo.rate-limit.login-limit=10",
        "demo.rate-limit.write-limit=60",
        "demo.rate-limit.window=PT1M",
        "cors.allowed-origins=http://localhost:5173"
})
class DemoSecurityFilterChainTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @Autowired private MockMvc mockMvc;
    @Autowired private DemoProperties properties;
    @Autowired private FilterRegistrationBean<DemoRequestGuardFilter> demoRequestGuardFilterRegistration;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private LoginAccountApi loginAccountApi;
    @MockitoBean private DemoProtectionPolicy protectionPolicy;
    @MockitoBean private DemoStateStore stateStore;
    @MockitoBean private AuthService authService;

    private final AtomicReference<String> authenticatedLoginId = new AtomicReference<>("demo.staff");

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
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
    @DisplayName("GET은 write guard와 rate limit 없이 통과")
    void authenticated_get_passes() throws Exception {
        authenticatedLoginId.set("reader-account");

        mockMvc.perform(get("/api/v1/probe")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());

        verify(protectionPolicy, never()).assertWriteAvailable();
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
    @DisplayName("guard servlet 자동 등록은 꺼져 SecurityFilterChain에서 한 번만 실행")
    void servlet_registration_is_disabled() {
        assertThat(demoRequestGuardFilterRegistration.isEnabled()).isFalse();
    }
}
