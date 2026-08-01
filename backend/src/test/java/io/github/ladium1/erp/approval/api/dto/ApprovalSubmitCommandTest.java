package io.github.ladium1.erp.approval.api.dto;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalSubmitCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("연동 도메인 결재 본문 4,000자 경계 검증")
    void content_length_boundary() {
        ApprovalSubmitCommand accepted = command("가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH));
        ApprovalSubmitCommand rejected = command("가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH + 1));

        assertThat(validator.validate(accepted)).isEmpty();
        assertThat(validator.validate(rejected))
                .singleElement()
                .satisfies(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("content"));
    }

    private ApprovalSubmitCommand command(String content) {
        return ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.GENERAL)
                .title("제목")
                .content(content)
                .drafterId(1L)
                .approverIds(List.of(2L))
                .build();
    }
}
