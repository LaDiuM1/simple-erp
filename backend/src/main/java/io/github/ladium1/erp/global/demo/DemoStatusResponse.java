package io.github.ladium1.erp.global.demo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

public record DemoStatusResponse(
        boolean enabled,
        String environmentName,
        DemoState state,
        OffsetDateTime stateChangedAt,
        String generation,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String candidateGeneration,
        OffsetDateTime lastResetAt,
        OffsetDateTime nextResetAt,
        long warningBeforeSeconds,
        long writeLockBeforeSeconds,
        boolean writeLocked,
        String notice,
        boolean uploadEnabled,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        DemoSimulatedLocation simulatedLocation,
        List<DemoPublicAccount> publicAccounts
) {
    public DemoStatusResponse {
        publicAccounts = publicAccounts == null ? List.of() : List.copyOf(publicAccounts);
    }
}
