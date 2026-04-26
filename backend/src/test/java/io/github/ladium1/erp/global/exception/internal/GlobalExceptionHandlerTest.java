package io.github.ladium1.erp.global.exception.internal;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @DisplayName("@Valid @RequestBody 검증 실패는 400 으로 응답되며 첫 필드 메시지가 노출된다")
    void handle_method_argument_not_valid() throws Exception {
        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: 이름은 필수입니다"));
    }

    @Test
    @DisplayName("ConstraintViolationException 은 400 으로 응답되며 필드명 + 메시지가 노출된다")
    void handle_constraint_violation() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: 이름은 필수입니다"));
    }

    @Test
    @DisplayName("잘못된 JSON 본문은 400 으로 응답된다")
    void handle_message_not_readable() throws Exception {
        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-a-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("요청 본문이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("경로 변수 타입 불일치는 400 으로 응답되며 파라미터 이름이 포함된다")
    void handle_type_mismatch() throws Exception {
        mockMvc.perform(get("/test/typed/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("요청 파라미터 형식이 올바르지 않습니다: id"));
    }

    @Test
    @DisplayName("필수 쿼리 파라미터 누락은 400 으로 응답되며 파라미터 이름이 포함된다")
    void handle_missing_parameter() throws Exception {
        mockMvc.perform(get("/test/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("필수 파라미터가 누락되었습니다: keyword"));
    }

    @Test
    @DisplayName("허용되지 않은 HTTP 메서드는 405 로 응답된다")
    void handle_method_not_supported() throws Exception {
        mockMvc.perform(put("/test/business-exception"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("허용되지 않은 HTTP 메서드입니다: PUT"));
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

    record TestPayload(@NotBlank(message = "이름은 필수입니다") String name) {}

    @RestController
    static class TestController {
        @GetMapping("/test/business-exception")
        public void throwBusiness() { throw new TestBusinessException(); }

        @PostMapping("/test/valid-body")
        public void validBody(@Valid @RequestBody TestPayload payload) { /* no-op */ }

        @GetMapping("/test/constraint-violation")
        public void throwConstraintViolation() {
            Set<jakarta.validation.ConstraintViolation<TestPayload>> violations = jakarta.validation.Validation
                    .buildDefaultValidatorFactory()
                    .getValidator()
                    .validate(new TestPayload(""));
            throw new ConstraintViolationException(violations);
        }

        @GetMapping("/test/typed/{id}")
        public void typedPath(@PathVariable Long id) { /* no-op */ }

        @GetMapping("/test/required-param")
        public void requiredParam(@RequestParam String keyword) { /* no-op */ }

        @GetMapping("/test/auth-exception")
        public void throwAuth() { throw new BadCredentialsException("인증 실패"); }

        @GetMapping("/test/access-exception")
        public void throwAccess() { throw new AccessDeniedException("권한 부족"); }

        @GetMapping("/test/runtime-exception")
        public void throwRuntime() { throw new RuntimeException("예상치 못한 시스템 오류"); }
    }
}
