package io.github.ladium1.erp.drive.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderCreateRequest(
        @NotBlank @Size(max = 100)
        String name,

        /**
         * 상위 폴더 식별자 — null 이면 루트에 생성.
         */
        Long parentId
) {
}
