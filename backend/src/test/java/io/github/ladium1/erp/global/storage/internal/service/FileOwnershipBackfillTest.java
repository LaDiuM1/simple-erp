package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFileStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileOwnershipBackfillTest {

    @Test
    @DisplayName("참조 없는 기존 파일은 PENDING, 단일 참조 파일은 CLAIMED로 복원")
    void plans_pending_and_claimed_backfill() {
        List<FileOwnershipBackfill.BackfillUpdate> updates = FileOwnershipBackfill.plan(
                List.of(row(1L), row(2L)),
                List.of(new FileOwnershipBackfill.FileReference(2L, FileOwner.boardPost(20L), 7L))
        );

        assertThat(updates).containsExactly(
                new FileOwnershipBackfill.BackfillUpdate(1L, StoredFileStatus.PENDING, null, 7L),
                new FileOwnershipBackfill.BackfillUpdate(
                        2L, StoredFileStatus.CLAIMED, FileOwner.boardPost(20L), 7L)
        );
    }

    @Test
    @DisplayName("경비와 결재 문서의 동일한 논리 소유자 참조는 하나로 취급")
    void accepts_duplicate_aliases_for_same_logical_owner() {
        FileOwner owner = FileOwner.expenseClaim(30L);

        List<FileOwnershipBackfill.BackfillUpdate> updates = FileOwnershipBackfill.plan(
                List.of(row(1L)),
                List.of(
                        new FileOwnershipBackfill.FileReference(1L, owner, 7L),
                        new FileOwnershipBackfill.FileReference(1L, owner, 7L)
                )
        );

        assertThat(updates).containsExactly(
                new FileOwnershipBackfill.BackfillUpdate(1L, StoredFileStatus.CLAIMED, owner, 7L));
    }

    @Test
    @DisplayName("하나의 파일이 서로 다른 업무 레코드에 연결되어 있으면 복원 중단")
    void rejects_conflicting_legacy_owners() {
        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(row(1L)),
                List.of(
                        new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L),
                        new FileOwnershipBackfill.FileReference(1L, FileOwner.approvalDocument(30L), 7L)
                )
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=1");
    }

    @Test
    @DisplayName("업무 레코드가 존재하지 않는 저장 파일을 참조하면 복원 중단")
    void rejects_dangling_legacy_reference() {
        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(),
                List.of(new FileOwnershipBackfill.FileReference(99L, FileOwner.driveFile(7L), 7L))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=99");
    }

    @Test
    @DisplayName("삭제 대기는 소유자만 유지하고 업무 참조가 없어야 함")
    void accepts_consistent_delete_pending_state() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, "DELETE_PENDING", "BOARD_POST", 20L, 7L);

        assertThat(FileOwnershipBackfill.plan(List.of(row), List.of())).isEmpty();
    }

    @Test
    @DisplayName("소유권 컬럼 일부만 남은 상태는 임의 복원하지 않음")
    void rejects_partial_recorded_owner() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, "CLAIMED", "BOARD_POST", null, 7L);

        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=1");
    }

    @Test
    @DisplayName("기존 업로더가 업무 소유자와 다르면 복원 중단")
    void rejects_uploader_conflict() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, null, null, null, 99L);

        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=1");
    }

    @Test
    @DisplayName("기존 업로더가 비어 있으면 업무 소유자 기준으로 함께 복원")
    void restores_missing_uploader_from_owner() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, null, null, null, null);

        assertThat(FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).containsExactly(new FileOwnershipBackfill.BackfillUpdate(
                1L, StoredFileStatus.CLAIMED, FileOwner.boardPost(20L), 7L));
    }

    @Test
    @DisplayName("구버전이 기본값 PENDING으로 저장한 단일 참조 파일을 CLAIMED로 승격")
    void promotes_rollback_created_pending_file() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, "PENDING", null, null, 7L);

        assertThat(FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).containsExactly(new FileOwnershipBackfill.BackfillUpdate(
                1L, StoredFileStatus.CLAIMED, FileOwner.boardPost(20L), 7L));
    }

    @Test
    @DisplayName("구버전 PENDING 파일의 업로더와 업무 소유자가 다르면 승격 중단")
    void rejects_rollback_created_pending_file_with_uploader_conflict() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, "PENDING", null, null, 99L);

        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=1");
    }

    @Test
    @DisplayName("구버전 PENDING 파일의 업로더를 확인할 수 없으면 승격 중단")
    void rejects_rollback_created_pending_file_without_uploader() {
        FileOwnershipBackfill.StoredRow row = new FileOwnershipBackfill.StoredRow(
                1L, "PENDING", null, null, null);

        assertThatThrownBy(() -> FileOwnershipBackfill.plan(
                List.of(row),
                List.of(new FileOwnershipBackfill.FileReference(1L, FileOwner.boardPost(20L), 7L))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fileId=1");
    }

    @Test
    @DisplayName("status 컬럼은 NOT NULL과 PENDING 기본값을 함께 유지")
    void detects_incomplete_status_constraint() {
        assertThat(FileOwnershipBackfill.requiresStatusConstraint("NO", "PENDING")).isFalse();
        assertThat(FileOwnershipBackfill.requiresStatusConstraint("NO", "'PENDING'")).isFalse();
        assertThat(FileOwnershipBackfill.requiresStatusConstraint("YES", "PENDING")).isTrue();
        assertThat(FileOwnershipBackfill.requiresStatusConstraint("NO", null)).isTrue();
    }

    private FileOwnershipBackfill.StoredRow row(Long id) {
        return new FileOwnershipBackfill.StoredRow(id, null, null, null, 7L);
    }
}
