package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.demo.DemoErrorCode;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.demo.DemoUploadQuotaGuard;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFileStatus;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @Mock private StoredFileRepository storedFileRepository;
    @Mock private DemoProtectionPolicy demoProtectionPolicy;
    @Mock private DemoUploadQuotaGuard demoUploadQuotaGuard;

    @TempDir
    Path tempDir;

    private static final Long FILE_ID = 1L;
    private static final Long UPLOADER_ID = 10L;
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 7, 10, 0);
    private static final FileOwner OWNER = FileOwner.boardPost(100L);

    private final AtomicReference<StoredFile> savedFile = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(
                storedFileRepository, demoProtectionPolicy, demoUploadQuotaGuard, tempDir.toString());
    }

    private void stubSaveWithJpaAudit() {
        stubSaveWithJpaAudit(CREATED_AT);
    }

    private void stubSaveWithJpaAudit(LocalDateTime createdAt) {
        willAnswer(invocation -> {
            StoredFile file = invocation.getArgument(0);
            ReflectionTestUtils.setField(file, "id", FILE_ID);
            ReflectionTestUtils.setField(file, "createdAt", createdAt);
            savedFile.set(file);
            return file;
        }).given(storedFileRepository).save(any(StoredFile.class));
    }

    private void claimSavedFile() {
        given(storedFileRepository.findAllByIdForUpdate(List.of(FILE_ID)))
                .willReturn(List.of(savedFile.get()));
        fileStorageService.claim(List.of(FILE_ID), OWNER, UPLOADER_ID);
    }

    @Test
    @DisplayName("업로드 파일을 소유자에 연결한 뒤 메타데이터와 본체 조회")
    void store_claim_and_load_success() {
        byte[] content = "hello file".getBytes(StandardCharsets.UTF_8);
        stubSaveWithJpaAudit();

        StoredFileInfo stored = fileStorageService.store("hello.txt", "text/plain", content, UPLOADER_ID);
        assertThat(savedFile.get().getStatus()).isEqualTo(StoredFileStatus.PENDING);

        claimSavedFile();
        given(storedFileRepository.findById(FILE_ID)).willReturn(Optional.of(savedFile.get()));

        StoredFileInfo info = fileStorageService.getInfo(FILE_ID, OWNER);
        byte[] loaded = fileStorageService.loadContent(FILE_ID, OWNER);

        assertThat(stored.id()).isEqualTo(FILE_ID);
        assertThat(stored.originalName()).isEqualTo("hello.txt");
        assertThat(stored.size()).isEqualTo(content.length);
        assertThat(stored.uploaderId()).isEqualTo(UPLOADER_ID);
        assertThat(savedFile.get().getStatus()).isEqualTo(StoredFileStatus.CLAIMED);
        assertThat(info.contentType()).isEqualTo("text/plain");
        assertThat(loaded).isEqualTo(content);
        assertThat(contentPathOf(savedFile.get())).exists();
    }

    @Test
    @DisplayName("같은 소유자가 같은 파일을 다시 연결해도 성공")
    void claim_is_idempotent_for_same_owner() {
        stubSaveWithJpaAudit();
        fileStorageService.store("hello.txt", "text/plain", "payload".getBytes(), UPLOADER_ID);
        given(storedFileRepository.findAllByIdForUpdate(List.of(FILE_ID)))
                .willReturn(List.of(savedFile.get()));

        fileStorageService.claim(List.of(FILE_ID), OWNER, UPLOADER_ID);
        fileStorageService.claim(List.of(FILE_ID), OWNER, UPLOADER_ID);

        assertThat(savedFile.get().isClaimedBy(OWNER)).isTrue();
    }

    @Test
    @DisplayName("다른 업로더나 다른 업무 소유자는 파일을 연결할 수 없음")
    void claim_rejects_uploader_and_owner_mismatch() {
        stubSaveWithJpaAudit();
        fileStorageService.store("hello.txt", "text/plain", "payload".getBytes(), UPLOADER_ID);
        given(storedFileRepository.findAllByIdForUpdate(List.of(FILE_ID)))
                .willReturn(List.of(savedFile.get()));

        assertThatThrownBy(() -> fileStorageService.claim(List.of(FILE_ID), OWNER, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);

        fileStorageService.claim(List.of(FILE_ID), OWNER, UPLOADER_ID);
        assertThatThrownBy(() -> fileStorageService.claim(
                List.of(FILE_ID), FileOwner.approvalDocument(100L), UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);
    }

    @Test
    @DisplayName("여러 파일 중 하나라도 연결할 수 없으면 어느 파일도 상태를 바꾸지 않음")
    void claim_validates_all_files_before_changing_state() {
        StoredFile first = pendingFile(1L, UPLOADER_ID);
        StoredFile second = pendingFile(2L, 99L);
        given(storedFileRepository.findAllByIdForUpdate(List.of(1L, 2L)))
                .willReturn(List.of(first, second));

        assertThatThrownBy(() -> fileStorageService.claim(List.of(2L, 1L), OWNER, UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);

        assertThat(first.getStatus()).isEqualTo(StoredFileStatus.PENDING);
        assertThat(second.getStatus()).isEqualTo(StoredFileStatus.PENDING);
    }

    @Test
    @DisplayName("연결 요청은 최대 20개이며 null과 중복을 허용하지 않음")
    void claim_validates_batch_shape_before_repository_access() {
        List<Long> overLimit = LongStream.rangeClosed(1, 21).boxed().toList();

        assertInvalidClaim(overLimit);
        assertInvalidClaim(Arrays.asList(1L, null));
        assertInvalidClaim(List.of(1L, 1L));

        verify(storedFileRepository, never()).findAllByIdForUpdate(any());
    }

    @Test
    @DisplayName("기대한 소유자가 다르면 파일 존재를 숨김")
    void read_rejects_unexpected_owner() {
        StoredFile file = claimedFile(OWNER);
        given(storedFileRepository.findById(FILE_ID)).willReturn(Optional.of(file));

        assertThatThrownBy(() -> fileStorageService.getInfo(FILE_ID, FileOwner.boardPost(200L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);
        assertThatThrownBy(() -> fileStorageService.loadContent(FILE_ID, FileOwner.boardPost(200L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제 요청은 소유권을 확인한 뒤 물리 정리 전까지 본체를 유지")
    void request_deletion_then_cleanup() throws IOException {
        stubSaveWithJpaAudit();
        fileStorageService.store("delete.txt", "text/plain", "delete me".getBytes(), UPLOADER_ID);
        claimSavedFile();
        Path contentPath = contentPathOf(savedFile.get());
        given(storedFileRepository.findAllByIdForUpdate(List.of(FILE_ID)))
                .willReturn(List.of(savedFile.get()));

        fileStorageService.requestDeletion(List.of(FILE_ID), OWNER);

        assertThat(savedFile.get().getStatus()).isEqualTo(StoredFileStatus.DELETE_PENDING);
        assertThat(contentPath).exists();

        given(storedFileRepository.findByStatusForUpdate(eq(StoredFileStatus.DELETE_PENDING), any(Pageable.class)))
                .willReturn(List.of(savedFile.get()));
        assertThat(fileStorageService.deletePendingFiles()).isEqualTo(1);

        assertThat(contentPath).doesNotExist();
        verify(storedFileRepository).deleteAll(List.of(savedFile.get()));
    }

    @Test
    @DisplayName("소유자가 다른 삭제 요청은 상태를 바꾸지 않음")
    void request_deletion_rejects_unexpected_owner() {
        StoredFile file = claimedFile(OWNER);
        given(storedFileRepository.findAllByIdForUpdate(List.of(FILE_ID))).willReturn(List.of(file));

        assertThatThrownBy(() -> fileStorageService.requestDeletion(
                List.of(FILE_ID), FileOwner.boardPost(200L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);

        assertThat(file.getStatus()).isEqualTo(StoredFileStatus.CLAIMED);
    }

    @Test
    @DisplayName("여러 파일 중 하나라도 소유권이 다르면 어느 파일도 삭제 대기로 바꾸지 않음")
    void request_deletion_validates_all_files_before_changing_state() {
        StoredFile first = pendingFile(1L, UPLOADER_ID);
        first.claim(OWNER);
        StoredFile second = pendingFile(2L, UPLOADER_ID);
        second.claim(FileOwner.boardPost(200L));
        given(storedFileRepository.findAllByIdForUpdate(List.of(1L, 2L)))
                .willReturn(List.of(first, second));

        assertThatThrownBy(() -> fileStorageService.requestDeletion(List.of(1L, 2L), OWNER))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);

        assertThat(first.getStatus()).isEqualTo(StoredFileStatus.CLAIMED);
        assertThat(second.getStatus()).isEqualTo(StoredFileStatus.CLAIMED);
    }

    @Test
    @DisplayName("물리 파일 정리에 실패하면 메타데이터를 남겨 다음 주기에 다시 시도")
    void cleanup_keeps_metadata_when_content_deletion_fails() throws IOException {
        StoredFile file = pendingFile();
        file.requestDeletion();
        Path contentPath = contentPathOf(file);
        Files.createDirectories(contentPath);
        Files.writeString(contentPath.resolve("child"), "not empty");
        given(storedFileRepository.findByStatusForUpdate(
                eq(StoredFileStatus.DELETE_PENDING), any(Pageable.class)))
                .willReturn(List.of(file));

        assertThatThrownBy(fileStorageService::deletePendingFiles)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.STORAGE_IO_FAILED);

        assertThat(contentPath).exists();
        verify(storedFileRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("TTL이 지난 미연결 업로드만 삭제 대기로 전환")
    void mark_expired_pending_for_deletion() {
        StoredFile pending = pendingFile();
        LocalDateTime threshold = CREATED_AT.plusDays(1);
        given(storedFileRepository.findCreatedBeforeForUpdate(
                eq(StoredFileStatus.PENDING), eq(threshold), any(Pageable.class)))
                .willReturn(List.of(pending));

        assertThat(fileStorageService.markExpiredPendingForDeletion(threshold)).isEqualTo(1);
        assertThat(pending.getStatus()).isEqualTo(StoredFileStatus.DELETE_PENDING);
    }

    @Test
    @DisplayName("파일명과 MIME이 비어도 재시작 검증 가능한 안전한 메타데이터로 정규화")
    void store_normalizes_missing_multipart_metadata() {
        stubSaveWithJpaAudit();

        StoredFileInfo stored = fileStorageService.store(
                " \t", null, "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);

        assertThat(stored.originalName()).isEqualTo("upload.bin");
        assertThat(stored.contentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        assertThat(savedFile.get().getOriginalName()).isEqualTo("upload.bin");
        assertThat(savedFile.get().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    @Test
    @DisplayName("공유 storage root 아래 새 연·월 디렉터리는 reset 가능한 2775와 group을 상속")
    void store_inherits_shared_directory_contract_for_new_year_and_month() throws IOException {
        Assumptions.assumeTrue(supportsUnixMode(tempDir));
        Files.setAttribute(tempDir, "unix:mode", 02775);
        long sharedGroup = ((Number) Files.getAttribute(tempDir, "unix:gid")).longValue();
        stubSaveWithJpaAudit();

        fileStorageService.store(
                "new-month.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);

        for (Path directory : List.of(tempDir.resolve("2026"), tempDir.resolve("2026/07"))) {
            assertThat(((Number) Files.getAttribute(directory, "unix:mode")).intValue() & 07777)
                    .isEqualTo(02775);
            assertThat(((Number) Files.getAttribute(directory, "unix:gid")).longValue())
                    .isEqualTo(sharedGroup);
        }
        Path content = contentPathOf(savedFile.get());
        assertThat(unixMode(content)).isEqualTo(0660);
        assertThat(((Number) Files.getAttribute(content, "unix:gid")).longValue())
                .isEqualTo(sharedGroup);
    }

    @Test
    @DisplayName("공유 계약이 없는 저장소는 업로드 파일의 기본 전용 권한을 유지")
    void store_keeps_private_file_mode_without_shared_root_contract() throws IOException {
        Assumptions.assumeTrue(supportsUnixMode(tempDir));
        Files.setAttribute(tempDir, "unix:mode", 0755);
        stubSaveWithJpaAudit();

        fileStorageService.store(
                "private.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);

        assertThat(unixMode(contentPathOf(savedFile.get()))).isEqualTo(0600);
    }

    @Test
    @DisplayName("공유 root의 기존 2775 연·월 디렉터리는 권한 변경 없이 업로드에 사용")
    void store_uses_existing_shared_directories_that_already_match_contract() throws IOException {
        Assumptions.assumeTrue(supportsUnixMode(tempDir));
        Files.setAttribute(tempDir, "unix:mode", 02775);
        Path year = Files.createDirectory(tempDir.resolve("2026"));
        Path month = Files.createDirectory(year.resolve("07"));
        Files.setAttribute(year, "unix:mode", 02775);
        Files.setAttribute(month, "unix:mode", 02775);
        stubSaveWithJpaAudit();

        fileStorageService.store(
                "canonical-month.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);

        assertThat(((Number) Files.getAttribute(year, "unix:mode")).intValue() & 07777)
                .isEqualTo(02775);
        assertThat(((Number) Files.getAttribute(month, "unix:mode")).intValue() & 07777)
                .isEqualTo(02775);
    }

    @Test
    @DisplayName("create와 chmod 사이 중단으로 남은 backend-owned 디렉터리는 다음 업로드가 복구")
    void store_repairs_backend_owned_directory_left_before_permission_fixup() throws IOException {
        Assumptions.assumeTrue(supportsUnixMode(tempDir));
        Files.setAttribute(tempDir, "unix:mode", 02775);
        Path year = Files.createDirectory(tempDir.resolve("2026"));
        Files.setAttribute(year, "unix:mode", 0755);
        stubSaveWithJpaAudit();

        fileStorageService.store(
                "recovered-month.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);

        assertThat(((Number) Files.getAttribute(year, "unix:mode")).intValue() & 07777)
                .isEqualTo(02775);
        assertThat(((Number) Files.getAttribute(year.resolve("07"), "unix:mode")).intValue() & 07777)
                .isEqualTo(02775);
    }

    @Test
    @DisplayName("Linux 공유 fixture에서 root-owned canonical 월과 신규 월 업로드 계약 유지")
    void store_uses_root_owned_canonical_month_and_creates_resettable_new_month() throws IOException {
        String fixture = System.getenv("ERP_STORAGE_PERMISSION_FIXTURE");
        Assumptions.assumeTrue(fixture != null && !fixture.isBlank());
        Path current = Path.of(fixture);
        Assumptions.assumeTrue(Files.isSymbolicLink(current) && supportsUnixMode(current));
        fileStorageService = new FileStorageService(
                storedFileRepository, demoProtectionPolicy, demoUploadQuotaGuard, current.toString());

        stubSaveWithJpaAudit(LocalDateTime.of(2026, 7, 7, 10, 0));
        fileStorageService.store(
                "canonical.txt", "text/plain", "canonical".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);
        Path canonicalContent = current.resolve("2026/07").resolve(savedFile.get().getStoredName());

        Path canonicalYear = current.resolve("2026");
        Path canonicalMonth = canonicalYear.resolve("07");
        assertThat(unixMode(canonicalYear)).isEqualTo(02775);
        assertThat(unixMode(canonicalMonth)).isEqualTo(02775);
        assertThat(unixMode(canonicalContent)).isEqualTo(0660);
        assertThat(((Number) Files.getAttribute(canonicalContent, "unix:gid")).longValue())
                .isEqualTo(((Number) Files.getAttribute(current, "unix:gid")).longValue());

        stubSaveWithJpaAudit(LocalDateTime.of(2026, 8, 7, 10, 0));
        fileStorageService.store(
                "new-month.txt", "text/plain", "new-month".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);
        Path newMonthContent = current.resolve("2026/08").resolve(savedFile.get().getStoredName());

        Path newMonth = canonicalYear.resolve("08");
        assertThat(unixMode(newMonth)).isEqualTo(02775);
        assertThat(((Number) Files.getAttribute(newMonth, "unix:gid")).longValue())
                .isEqualTo(((Number) Files.getAttribute(current, "unix:gid")).longValue());
        assertThat(unixMode(newMonthContent)).isEqualTo(0660);
        assertThat(((Number) Files.getAttribute(newMonthContent, "unix:gid")).longValue())
                .isEqualTo(((Number) Files.getAttribute(current, "unix:gid")).longValue());
    }

    @Test
    @DisplayName("상위 Drive 트랜잭션까지 포함해 rollback이면 이미 이동한 파일 본체 제거")
    void store_removes_materialized_content_on_outer_transaction_rollback() {
        stubSaveWithJpaAudit();
        TransactionSynchronizationManager.initSynchronization();
        try {
            fileStorageService.store(
                    "drive.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);
            Path contentPath = contentPathOf(savedFile.get());
            assertThat(contentPath).exists();
            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

            completeSynchronization(TransactionSynchronization.STATUS_ROLLED_BACK);

            assertThat(contentPath).doesNotExist();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("트랜잭션 commit 뒤에는 원자적으로 이동한 파일 본체 유지")
    void store_keeps_materialized_content_after_transaction_commit() {
        stubSaveWithJpaAudit();
        TransactionSynchronizationManager.initSynchronization();
        try {
            fileStorageService.store(
                    "committed.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID);
            Path contentPath = contentPathOf(savedFile.get());

            completeSynchronization(TransactionSynchronization.STATUS_COMMITTED);

            assertThat(contentPath).hasContent("payload");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("파일 기록 중 IOException이 나면 부분 본체를 남기지 않고 저장 실패")
    void store_removes_partial_content_after_io_failure() throws IOException {
        stubSaveWithJpaAudit();
        fileStorageService = new FileStorageService(
                storedFileRepository,
                demoProtectionPolicy,
                demoUploadQuotaGuard,
                tempDir.toString(),
                (target, content, sharedStorage) -> {
                    Files.createDirectories(target.getParent());
                    Files.write(target, Arrays.copyOf(content, 2));
                    throw new IOException("simulated partial write");
                }
        );

        assertThatThrownBy(() -> fileStorageService.store(
                "partial.txt", "text/plain", "payload".getBytes(StandardCharsets.UTF_8), UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.STORAGE_IO_FAILED);

        assertThat(regularFiles()).isEmpty();
    }

    @Test
    @DisplayName("빈 파일 업로드 거부")
    void store_fail_empty_content() {
        assertThatThrownBy(() -> fileStorageService.store("empty.txt", "text/plain", new byte[0], UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.EMPTY_FILE);

        verify(storedFileRepository, never()).save(any(StoredFile.class));
    }

    @Test
    @DisplayName("데모 누적 quota는 metadata와 파일을 만들기 전에 차단")
    void store_checks_demo_quota_before_side_effects() {
        byte[] content = "blocked".getBytes(StandardCharsets.UTF_8);
        willThrow(new BusinessException(DemoErrorCode.DEMO_UPLOAD_QUOTA_EXCEEDED))
                .given(demoUploadQuotaGuard)
                .assertUploadAllowed(UPLOADER_ID, content.length, tempDir);

        assertThatThrownBy(() -> fileStorageService.store(
                "quota.txt", "text/plain", content, UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        DemoErrorCode.DEMO_UPLOAD_QUOTA_EXCEEDED
                );

        verify(storedFileRepository, never()).save(any(StoredFile.class));
        assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    @DisplayName("파일 본체를 쓰기 직전에 실제 generation 디스크 여유를 다시 확인")
    void store_rechecks_disk_reserve_immediately_before_content_write() {
        byte[] content = "payload".getBytes(StandardCharsets.UTF_8);
        stubSaveWithJpaAudit();

        fileStorageService.store("safe.txt", "text/plain", content, UPLOADER_ID);

        verify(demoUploadQuotaGuard)
                .assertUploadAllowed(UPLOADER_ID, content.length, tempDir);
        verify(demoUploadQuotaGuard)
                .assertDiskReserve(tempDir.resolve("2026/07"), content.length);
        assertThat(contentPathOf(savedFile.get())).exists();
    }

    @Test
    @DisplayName("데모 업로드 정책은 파일 IO와 DB 저장 전에 최종 차단")
    void store_obeys_demo_policy_before_side_effects() {
        willThrow(new BusinessException(DemoErrorCode.DEMO_UPLOAD_DISABLED))
                .given(demoProtectionPolicy).assertUploadAllowed();

        assertThatThrownBy(() -> fileStorageService.store(
                "blocked.txt", "text/plain", "blocked".getBytes(StandardCharsets.UTF_8), UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DemoErrorCode.DEMO_UPLOAD_DISABLED);

        verify(storedFileRepository, never()).save(any(StoredFile.class));
        assertThat(tempDir).isEmptyDirectory();
    }

    private void assertInvalidClaim(List<Long> fileIds) {
        assertThatThrownBy(() -> fileStorageService.claim(fileIds, OWNER, UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.INVALID_FILE_REFERENCES);
    }

    private StoredFile pendingFile() {
        return pendingFile(FILE_ID, UPLOADER_ID);
    }

    private StoredFile pendingFile(Long fileId, Long uploaderId) {
        StoredFile file = StoredFile.builder()
                .originalName("file.txt")
                .storedName("stored-name")
                .contentType("text/plain")
                .size(7)
                .uploaderId(uploaderId)
                .build();
        ReflectionTestUtils.setField(file, "id", fileId);
        ReflectionTestUtils.setField(file, "createdAt", CREATED_AT);
        return file;
    }

    private StoredFile claimedFile(FileOwner owner) {
        StoredFile file = pendingFile();
        file.claim(owner);
        return file;
    }

    @Test
    @DisplayName("데모 세대에서는 삭제 예약 파일의 본체를 초기화 전까지 보존")
    void delete_pending_files_retains_demo_generation() {
        given(demoProtectionPolicy.shouldRetainStoredFiles()).willReturn(true);

        assertThat(fileStorageService.deletePendingFiles()).isZero();

        verify(storedFileRepository, never()).findByStatusForUpdate(any(), any(Pageable.class));
        verify(storedFileRepository, never()).deleteAll(any());
    }

    private Path contentPathOf(StoredFile file) {
        return tempDir.resolve("2026").resolve("07").resolve(file.getStoredName());
    }

    private List<Path> regularFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(tempDir)) {
            return paths.filter(Files::isRegularFile).toList();
        }
    }

    private static boolean supportsUnixMode(Path path) {
        try {
            return Files.getAttribute(path, "unix:mode") instanceof Number
                    && Files.getAttribute(path, "unix:gid") instanceof Number;
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException unsupported) {
            return false;
        }
    }

    private static int unixMode(Path path) throws IOException {
        return ((Number) Files.getAttribute(path, "unix:mode")).intValue() & 07777;
    }

    private static void completeSynchronization(int status) {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(status));
    }
}
