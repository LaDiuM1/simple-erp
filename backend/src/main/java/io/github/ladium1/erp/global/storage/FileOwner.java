package io.github.ladium1.erp.global.storage;

import java.util.Objects;

/** 파일의 독점 논리 소유자. */
public record FileOwner(FileOwnerType type, Long id) {

    public FileOwner {
        Objects.requireNonNull(type, "파일 소유자 유형은 필수입니다.");
        Objects.requireNonNull(id, "파일 소유자 식별자는 필수입니다.");
        if (id <= 0) {
            throw new IllegalArgumentException("파일 소유자 식별자는 양수여야 합니다.");
        }
    }

    public static FileOwner boardPost(Long id) {
        return new FileOwner(FileOwnerType.BOARD_POST, id);
    }

    public static FileOwner approvalDocument(Long id) {
        return new FileOwner(FileOwnerType.APPROVAL_DOCUMENT, id);
    }

    public static FileOwner expenseClaim(Long id) {
        return new FileOwner(FileOwnerType.EXPENSE_CLAIM, id);
    }

    public static FileOwner driveFile(Long id) {
        return new FileOwner(FileOwnerType.DRIVE_FILE, id);
    }
}
