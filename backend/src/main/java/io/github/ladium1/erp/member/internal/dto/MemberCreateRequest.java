package io.github.ladium1.erp.member.internal.dto;

import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberCreateRequest(
        @NotBlank @Size(max = 50)
        String loginId,

        @NotBlank @Size(min = 8, max = 100)
        String password,

        @NotBlank @Size(max = 50)
        String name,

        @Email @Size(max = 100)
        String email,

        @Size(max = 20)
        String phone,

        @Size(max = 10)
        String zipCode,

        @Size(max = 200)
        String roadAddress,

        @Size(max = 200)
        String detailAddress,

        LocalDate joinDate,

        @NotNull
        MemberStatus status,

        @NotNull
        Long roleId,

        Long departmentId,

        Long positionId
) {
}
