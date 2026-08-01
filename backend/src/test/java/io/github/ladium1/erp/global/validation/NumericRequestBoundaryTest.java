package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceUpdateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.expense.internal.dto.ExpenseCreateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NumericRequestBoundaryTest {

    private static final BigDecimal EXTREME_POSITIVE_EXPONENT = new BigDecimal("1E+2147483647");

    @Test
    @DisplayName("DB decimal 입력은 작은 JSON으로 표현 가능한 과대 지수를 거부")
    void decimal_requests_reject_extreme_positive_exponents() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertInvalid(validator, ExpenseCreateRequest.ItemRequest.class, "amount");
            assertInvalid(validator, ContractCreateRequest.class, "outputValue");
            assertInvalid(validator, ContractUpdateRequest.class, "outputValue");
            assertInvalid(validator, EquipmentCreateRequest.class, "outputValue");
            assertInvalid(validator, EquipmentUpdateRequest.class, "outputValue");
            assertInvalid(validator, LeaveBalanceUpdateRequest.class, "grantedDays");
        }
    }

    private static <T> void assertInvalid(Validator validator, Class<T> type, String property) {
        assertThat(validator.validateValue(type, property, EXTREME_POSITIVE_EXPONENT))
                .as("%s.%s", type.getSimpleName(), property)
                .isNotEmpty();
    }
}
