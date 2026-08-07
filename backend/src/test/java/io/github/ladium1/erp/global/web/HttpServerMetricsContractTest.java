package io.github.ladium1.erp.global.web;

import io.micrometer.common.KeyValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class HttpServerMetricsContractTest {

    private final DefaultServerRequestObservationConvention convention =
            new DefaultServerRequestObservationConvention();

    @Test
    @DisplayName("식별자가 다른 요청은 같은 route 템플릿 URI tag 사용")
    void uses_normalized_route_template_for_uri_tag() {
        String routeTemplate = "/api/v1/customers/{id}";

        String firstUriTag = uriTag("/api/v1/customers/1", routeTemplate);
        String secondUriTag = uriTag("/api/v1/customers/999999", routeTemplate);

        assertThat(firstUriTag).isEqualTo(routeTemplate);
        assertThat(secondUriTag).isEqualTo(routeTemplate);
        assertThat(firstUriTag).doesNotEndWith("/1");
        assertThat(secondUriTag).doesNotEndWith("/999999");
    }

    private String uriTag(String requestUri, String routeTemplate) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        ServerRequestObservationContext context = new ServerRequestObservationContext(
                request, new MockHttpServletResponse());
        context.setPathPattern(routeTemplate);

        return StreamSupport.stream(convention.getLowCardinalityKeyValues(context).spliterator(), false)
                .filter(keyValue -> "uri".equals(keyValue.getKey()))
                .map(KeyValue::getValue)
                .findFirst()
                .orElseThrow();
    }
}
