package io.github.ladium1.erp.salescontact.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SalesContactUpdateRequest(
        @NotBlank @Size(max = 50)
        String name,

        @Size(max = 50)
        String nameEn,

        @Size(max = 30)
        String mobilePhone,

        @Size(max = 30)
        String officePhone,

        @Email @Size(max = 100)
        String email,

        @Email @Size(max = 100)
        String personalEmail,

        LocalDate metAt,

        List<Long> sourceIds,

        String note
) {
}
