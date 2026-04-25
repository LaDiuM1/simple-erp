package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeUpdateRequest(
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
        EmployeeStatus status,

        @NotNull
        Long roleId,

        Long departmentId,

        Long positionId,

        /** 비어있으면 (null) 비밀번호 유지. 입력되면 4자 이상이어야 함. */
        @Size(min = 4, max = 100)
        String newPassword
) {
}
