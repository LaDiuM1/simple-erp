package io.github.ladium1.erp.approval.internal.dto;

import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * GENERAL 기안 작성 요청 — 기안자는 현재 사용자.
 */
public record ApprovalCreateRequest(
        @NotBlank String title,
        @Size(max = RequestTextPolicy.MAX_LONG_TEXT_LENGTH)
        String content,
        @NotEmpty @Size(max = RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE)
        List<@NotNull Long> approverIds,
        List<Long> attachmentFileIds
) {
}
