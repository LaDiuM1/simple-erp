package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceCreateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceUpdateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DemoExcelTextBoundaryValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest(name = "{0}.{1}")
    @MethodSource("exportedTextFields")
    @DisplayName("Excel에 실리는 create/update 자유 TEXT는 4000자 허용, 4001자 거절")
    void exported_text_fields_have_symmetric_bounds(Class<?> requestType, String fieldName)
            throws ReflectiveOperationException {
        Object boundary = recordWith(requestType, fieldName, "x".repeat(4000));
        Object oversized = recordWith(requestType, fieldName, "x".repeat(4001));

        assertThat(validator.validate(boundary))
                .noneMatch(violation -> violation.getPropertyPath().toString().equals(fieldName));
        assertThat(validator.validate(oversized))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(fieldName));
    }

    private static Stream<Arguments> exportedTextFields() {
        return Stream.of(
                Arguments.of(AfterServiceCreateRequest.class, "symptom"),
                Arguments.of(AfterServiceUpdateRequest.class, "symptom"),
                Arguments.of(ContractCreateRequest.class, "optionText"),
                Arguments.of(ContractUpdateRequest.class, "optionText"),
                Arguments.of(CustomerCreateRequest.class, "note"),
                Arguments.of(CustomerUpdateRequest.class, "note"),
                Arguments.of(EquipmentCreateRequest.class, "note"),
                Arguments.of(EquipmentUpdateRequest.class, "note"),
                Arguments.of(SalesContactCreateRequest.class, "note"),
                Arguments.of(SalesContactUpdateRequest.class, "note"),
                Arguments.of(ServiceVisitRequest.class, "problem"),
                Arguments.of(ServiceVisitRequest.class, "resolution")
        );
    }

    private static Object recordWith(Class<?> recordType, String fieldName, String value)
            throws ReflectiveOperationException {
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] parameterTypes = Stream.of(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        Object[] arguments = new Object[components.length];
        for (int index = 0; index < components.length; index++) {
            if (components[index].getName().equals(fieldName)) {
                arguments[index] = value;
            }
        }
        Constructor<?> constructor = recordType.getDeclaredConstructor(parameterTypes);
        return constructor.newInstance(arguments);
    }
}
