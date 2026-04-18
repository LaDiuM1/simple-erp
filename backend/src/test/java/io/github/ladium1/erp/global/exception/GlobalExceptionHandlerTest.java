package io.github.ladium1.erp.global.exception;

import io.github.ladium1.erp.global.exception.core.GlobalExceptionHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("비즈니스 예외 처리")
    void handleBusinessException_Success() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("TEST-400"))
                .andExpect(jsonPath("$.message").value("비즈니스 실패 메시지"));
    }

    @Test
    @DisplayName("인증 예외(401) 처리")
    void handleAuthenticationException_Success() throws Exception {
        mockMvc.perform(get("/test/auth-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH-401-000"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("인가 예외(403) 처리")
    void handleAccessDeniedException_Success() throws Exception {
        mockMvc.perform(get("/test/access-exception"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("AUTH-403-000"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("시스템 기타 예외(500) 처리")
    void handleException_Success() throws Exception {
        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("SYSTEM-500-000"))
                .andExpect(jsonPath("$.message").value("서버 시스템 오류가 발생했습니다."));
    }

    // --- 테스트를 위한 Dummy 객체 및 컨트롤러 ---

    @Getter
    @RequiredArgsConstructor
    enum TestErrorCode implements ErrorCode {
        TEST_ERROR(HttpStatus.BAD_REQUEST, "TEST-400", "비즈니스 실패 메시지");
        private final HttpStatus status;
        private final String code;
        private final String message;
    }

    static class TestBusinessException extends BusinessException {
        public TestBusinessException() {
            super(TestErrorCode.TEST_ERROR);
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/business-exception")
        public void throwBusiness() { throw new TestBusinessException(); }

        @GetMapping("/test/auth-exception")
        public void throwAuth() { throw new BadCredentialsException("인증 실패"); }

        @GetMapping("/test/access-exception")
        public void throwAccess() { throw new AccessDeniedException("권한 부족"); }

        @GetMapping("/test/runtime-exception")
        public void throwRuntime() { throw new RuntimeException("예상치 못한 시스템 오류"); }
    }
}