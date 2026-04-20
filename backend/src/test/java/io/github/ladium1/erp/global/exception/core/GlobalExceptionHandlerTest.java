package io.github.ladium1.erp.global.exception.core;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.exception.ErrorCode;
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
    void handle_business_exception() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("비즈니스 실패 메시지"));
    }

    @Test
    @DisplayName("인증(401) 예외 처리")
    void handle_authentication_exception() throws Exception {
        mockMvc.perform(get("/test/auth-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("인가(403) 예외 처리")
    void handle_access_denied_exception() throws Exception {
        mockMvc.perform(get("/test/access-exception"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("시스템 기타(500) 예외 처리")
    void handle_exception() throws Exception {
        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("서버 시스템 오류가 발생했습니다."));
    }

    @Getter
    @RequiredArgsConstructor
    enum TestErrorCode implements ErrorCode {
        TEST_ERROR(HttpStatus.BAD_REQUEST, "비즈니스 실패 메시지");
        private final HttpStatus status;
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