package io.github.ladium1.erp.salescontact.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SalesContactCreateRequest(
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

        /** 컨택 경로 (acquisition_sources) 식별자 목록 — 한 명함을 여러 경로로 받은 경우 다중 지정. */
        List<Long> sourceIds,

        String note
) {
}
