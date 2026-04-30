package io.github.ladium1.erp.salescustomer.internal.dto;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SalesActivityUpdateRequest(
        @NotNull
        SalesActivityType type,

        @NotNull
        LocalDateTime activityDate,

        @NotBlank @Size(max = 200)
        String subject,

        String content,

        @NotNull
        Long ourEmployeeId,

        /** 영업 명부 식별자 */
        Long customerContactId
) {
}
