package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DemoStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
class DemoStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemoStateStore stateStore;

    @Test
    @DisplayName("status는 기존 ApiResponse envelope 안에 frontend 계약을 반환")
    void returns_frontend_contract_in_envelope() throws Exception {
        given(stateStore.current()).willReturn(new DemoStatusResponse(
                true,
                "DEMO",
                DemoState.READY,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"),
                "generation-1",
                null,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"),
                OffsetDateTime.parse("2026-08-02T18:00:00+09:00"),
                300,
                120,
                false,
                "합성 데이터",
                false,
                new DemoSimulatedLocation(37.5663, 126.9779),
                List.of(new DemoPublicAccount(
                        "관리자", "전체 흐름", "demo.manager", "public-password", true))
        ));

        mockMvc.perform(get("/api/v1/demo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.environmentName").value("DEMO"))
                .andExpect(jsonPath("$.data.generation").value("generation-1"))
                .andExpect(jsonPath("$.data.warningBeforeSeconds").value(300))
                .andExpect(jsonPath("$.data.writeLockBeforeSeconds").value(120))
                .andExpect(jsonPath("$.data.simulatedLocation.latitude").value(37.5663))
                .andExpect(jsonPath("$.data.publicAccounts[0].loginId").value("demo.manager"))
                .andExpect(jsonPath("$.data.publicAccounts[0].password").value("public-password"));
    }

    @Test
    @DisplayName("nullable 필드는 null로 유지하고 optional 필드만 생략")
    void preserves_nullable_and_optional_field_contract() throws Exception {
        given(stateStore.current()).willReturn(new DemoStatusResponse(
                false,
                "PRODUCTION",
                DemoState.READY,
                null,
                null,
                null,
                null,
                null,
                300,
                120,
                false,
                "",
                true,
                null,
                List.of()
        ));

        mockMvc.perform(get("/api/v1/demo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generation").value(nullValue()))
                .andExpect(jsonPath("$.data.stateChangedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.lastResetAt").value(nullValue()))
                .andExpect(jsonPath("$.data.nextResetAt").value(nullValue()))
                .andExpect(jsonPath("$.data.candidateGeneration").doesNotExist())
                .andExpect(jsonPath("$.data.simulatedLocation").doesNotExist());
    }
}
