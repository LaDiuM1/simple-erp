package io.github.ladium1.erp.board.internal.dto;

import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record PostUpdateRequest(
        @NotNull
        BoardCategory category,

        @NotBlank @Size(max = 200)
        String title,

        @NotBlank @Size(max = RequestTextPolicy.MAX_LONG_TEXT_LENGTH)
        String content,

        @Size(max = RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE)
        @UniqueElements
        List<@NotNull Long> attachmentFileIds
) {
}
