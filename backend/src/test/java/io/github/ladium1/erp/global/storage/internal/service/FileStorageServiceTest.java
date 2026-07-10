package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @Mock private StoredFileRepository storedFileRepository;

    @TempDir
    Path tempDir;

    private static final Long FILE_ID = 1L;
    private static final Long UPLOADER_ID = 10L;
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 7, 10, 0);

    private final AtomicReference<StoredFile> savedFile = new AtomicReference<>();

    @BeforeEach
    void set_up() {
        fileStorageService = new FileStorageService(storedFileRepository, tempDir.toString());
    }

    // JPA 감사가 채우는 id / createdAt 을 save 시점에 시뮬레이션
    private void stubSaveWithJpaAudit() {
        given(storedFileRepository.save(any(StoredFile.class))).willAnswer(invocation -> {
            StoredFile file = invocation.getArgument(0);
            ReflectionTestUtils.setField(file, "id", FILE_ID);
            ReflectionTestUtils.setField(file, "createdAt", CREATED_AT);
            savedFile.set(file);
            return file;
        });
    }

    @Test
    @DisplayName("store → getInfo → loadContent 왕복 성공")
    void store_get_info_load_content_success() {
        // given
        byte[] content = "hello file".getBytes(StandardCharsets.UTF_8);
        stubSaveWithJpaAudit();

        // when
        StoredFileInfo stored = fileStorageService.store("hello.txt", "text/plain", content, UPLOADER_ID);
        given(storedFileRepository.findById(FILE_ID)).willReturn(Optional.of(savedFile.get()));
        StoredFileInfo info = fileStorageService.getInfo(FILE_ID);
        byte[] loaded = fileStorageService.loadContent(FILE_ID);

        // then
        assertThat(stored.id()).isEqualTo(FILE_ID);
        assertThat(stored.originalName()).isEqualTo("hello.txt");
        assertThat(stored.size()).isEqualTo(content.length);
        assertThat(stored.uploaderId()).isEqualTo(UPLOADER_ID);
        assertThat(info.contentType()).isEqualTo("text/plain");
        assertThat(loaded).isEqualTo(content);
        assertThat(tempDir.resolve("2026").resolve("07").resolve(savedFile.get().getStoredName())).exists();
    }

    @Test
    @DisplayName("빈 파일 업로드 거부")
    void store_fail_empty_content() {
        // when & then
        assertThatThrownBy(() -> fileStorageService.store("empty.txt", "text/plain", new byte[0], UPLOADER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.EMPTY_FILE);

        verify(storedFileRepository, never()).save(any(StoredFile.class));
    }

    @Test
    @DisplayName("존재하지 않는 파일 조회 시 404")
    void get_info_fail_file_not_found() {
        // given
        given(storedFileRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileStorageService.getInfo(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 메타 + 본체 제거 후 조회 실패")
    void delete_success_then_get_info_fail() {
        // given
        byte[] content = "delete me".getBytes(StandardCharsets.UTF_8);
        stubSaveWithJpaAudit();
        fileStorageService.store("delete.txt", "text/plain", content, UPLOADER_ID);
        StoredFile saved = savedFile.get();
        Path contentPath = tempDir.resolve("2026").resolve("07").resolve(saved.getStoredName());
        given(storedFileRepository.findById(FILE_ID))
                .willReturn(Optional.of(saved))
                .willReturn(Optional.empty());

        // when
        fileStorageService.delete(FILE_ID);

        // then
        verify(storedFileRepository).delete(saved);
        assertThat(contentPath).doesNotExist();
        assertThatThrownBy(() -> fileStorageService.getInfo(FILE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 파일 delete 는 무시")
    void delete_ignores_missing_file() {
        // given
        given(storedFileRepository.findById(99L)).willReturn(Optional.empty());

        // when
        fileStorageService.delete(99L);

        // then
        verify(storedFileRepository, never()).delete(any(StoredFile.class));
    }
}
