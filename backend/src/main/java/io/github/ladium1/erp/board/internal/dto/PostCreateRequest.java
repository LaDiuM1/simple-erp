package io.github.ladium1.erp.board.internal.dto;

import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotNull
        BoardCategory category,

        @NotBlank @Size(max = 200)
        String title,

        @NotBlank
        String content,

        List<Long> attachmentFileIds
) {
}
