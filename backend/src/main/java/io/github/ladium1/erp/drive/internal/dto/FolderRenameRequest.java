package io.github.ladium1.erp.drive.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderRenameRequest(
        @NotBlank @Size(max = 100)
        String name
) {
}
