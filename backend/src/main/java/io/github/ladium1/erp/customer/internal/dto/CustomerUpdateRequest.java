package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerUpdateRequest(
        @NotBlank @Size(max = 200)
        String name,

        @Size(max = 200)
        String nameEn,

        @Size(max = 20)
        String bizRegNo,

        @Size(max = 20)
        String corpRegNo,

        @Size(max = 50)
        String representative,

        @Size(max = 100)
        String bizType,

        @Size(max = 100)
        String bizItem,

        @Size(max = 30)
        String phone,

        @Size(max = 30)
        String fax,

        @Email @Size(max = 100)
        String email,

        @Size(max = 200)
        String website,

        @Size(max = 10)
        String zipCode,

        @Size(max = 200)
        String roadAddress,

        @Size(max = 200)
        String detailAddress,

        @NotNull
        CustomerType type,

        @NotNull
        CustomerStatus status,

        LocalDate tradeStartDate,

        String note
) {
}
