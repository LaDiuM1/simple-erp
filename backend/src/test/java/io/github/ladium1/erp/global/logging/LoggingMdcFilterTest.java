package io.github.ladium1.erp.global.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoggingMdcFilterTest {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID  = "userId";

    private final LoggingMdcFilter filter = new LoggingMdcFilter();

    @AfterEach
    void clear_thread_locals() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("traceId 부착 성공")
    void put_trace_id_success() throws Exception {
        // given
        String[] captured = new String[1];
        FilterChain chain = chainCapturing(TRACE_ID, captured);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        assertThat(captured[0]).isNotNull();
    }

    @Test
    @DisplayName("traceId 8자리 단축")
    void trace_id_eight_characters() throws Exception {
        // given
        String[] captured = new String[1];
        FilterChain chain = chainCapturing(TRACE_ID, captured);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        assertThat(captured[0]).hasSize(8);
    }

    @Test
    @DisplayName("traceId 요청별 유일성")
    void trace_id_unique_per_request() throws Exception {
        // given & when
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String[] captured = new String[1];
            FilterChain chain = chainCapturing(TRACE_ID, captured);
            new LoggingMdcFilter().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
            seen.add(captured[0]);
        }

        // then
        assertThat(seen).hasSize(10);
    }

    @Test
    @DisplayName("X-Trace-Id 응답 헤더 부착")
    void put_trace_id_response_header() throws Exception {
        // given
        String[] mdcCaptured = new String[1];
        FilterChain chain = chainCapturing(TRACE_ID, mdcCaptured);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        // then
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo(mdcCaptured[0]);
    }

    @Test
    @DisplayName("userId 부착 — 인증 사용자")
    void put_user_id_when_authenticated() throws Exception {
        // given
        authenticate("alice");
        String[] captured = new String[1];
        FilterChain chain = chainCapturing(USER_ID, captured);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        assertThat(captured[0]).isEqualTo("alice");
    }

    @Test
    @DisplayName("userId 미부착 — 미인증 요청")
    void skip_user_id_when_unauthenticated() throws Exception {
        // given
        String[] captured = new String[1];
        FilterChain chain = chainCapturing(USER_ID, captured);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        assertThat(captured[0]).isNull();
    }

    @Test
    @DisplayName("MDC 정리 — 정상 진행 후")
    void clear_mdc_after_normal_chain() throws Exception {
        // given
        authenticate("bob");
        FilterChain chain = mock(FilterChain.class);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        assertThat(MDC.get(TRACE_ID)).isNull();
        assertThat(MDC.get(USER_ID)).isNull();
    }

    @Test
    @DisplayName("MDC 정리 — 체인 예외 시")
    void clear_mdc_on_chain_exception() throws Exception {
        // given
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("boom")).when(chain).doFilter(any(), any());

        // when & then
        assertThatThrownBy(() ->
                filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain))
                .isInstanceOf(RuntimeException.class);
        assertThat(MDC.get(TRACE_ID)).isNull();
        assertThat(MDC.get(USER_ID)).isNull();
    }

    @Test
    @DisplayName("filterChain 1회 호출")
    void chain_called_exactly_once() throws Exception {
        // given
        FilterChain chain = mock(FilterChain.class);

        // when
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // then
        verify(chain, times(1)).doFilter(any(), any());
    }


    private static FilterChain chainCapturing(String mdcKey, String[] dest) throws Exception {
        FilterChain chain = mock(FilterChain.class);
        doAnswer(captureMdc(mdcKey, dest)).when(chain).doFilter(any(), any());
        return chain;
    }

    private static Answer<Object> captureMdc(String key, String[] dest) {
        return invocation -> {
            dest[0] = MDC.get(key);
            return null;
        };
    }

    private static void authenticate(String username) {
        User principal = new User(username, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities())
        );
    }
}
