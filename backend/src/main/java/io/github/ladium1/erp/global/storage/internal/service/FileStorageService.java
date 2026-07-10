package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FileStorageService implements FileStorageApi {

    private final StoredFileRepository storedFileRepository;
    private final Path basePath;

    public FileStorageService(
            StoredFileRepository storedFileRepository,
            @Value("${erp.storage.local.base-path}") String basePath
    ) {
        this.storedFileRepository = storedFileRepository;
        this.basePath = Path.of(basePath);
    }

    @Override
    @Transactional
    public StoredFileInfo store(String originalName, String contentType, byte[] content, Long uploaderId) {
        if (content == null || content.length == 0) {
            throw new BusinessException(StorageErrorCode.EMPTY_FILE);
        }

        StoredFile file = StoredFile.builder()
                .originalName(originalName)
                .storedName(UUID.randomUUID().toString())
                .contentType(contentType)
                .size(content.length)
                .uploaderId(uploaderId)
                .build();
        StoredFile saved = storedFileRepository.save(file);

        // 저장 실패 (STORAGE_IO_FAILED) 시 런타임 예외로 메타 INSERT 도 함께 롤백
        writeContent(saved, content);
        return toInfo(saved);
    }

    @Override
    public StoredFileInfo getInfo(Long fileId) {
        return toInfo(getStoredFile(fileId));
    }

    @Override
    public List<StoredFileInfo> getInfos(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        return storedFileRepository.findAllById(fileIds).stream()
                .map(this::toInfo)
                .toList();
    }

    @Override
    public byte[] loadContent(Long fileId) {
        StoredFile file = getStoredFile(fileId);
        try {
            return Files.readAllBytes(resolveContentPath(file));
        } catch (IOException e) {
            throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
        }
    }

    @Override
    @Transactional
    public void delete(Long fileId) {
        storedFileRepository.findById(fileId).ifPresent(file -> {
            storedFileRepository.delete(file);
            try {
                Files.deleteIfExists(resolveContentPath(file));
            } catch (IOException e) {
                throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
            }
        });
    }

    private StoredFile getStoredFile(Long fileId) {
        return storedFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(StorageErrorCode.FILE_NOT_FOUND));
    }

    private void writeContent(StoredFile file, byte[] content) {
        try {
            Path path = resolveContentPath(file);
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (IOException e) {
            throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
        }
    }

    /**
     * 본체 경로 = {base-path}/{yyyy}/{MM}/{storedName}.
     * 연/월은 createdAt 기준 — save 시점에 JPA 감사가 채우므로 저장/로드 양쪽에서 동일하게 재현된다.
     */
    private Path resolveContentPath(StoredFile file) {
        LocalDateTime createdAt = file.getCreatedAt();
        return basePath
                .resolve(String.format("%04d", createdAt.getYear()))
                .resolve(String.format("%02d", createdAt.getMonthValue()))
                .resolve(file.getStoredName());
    }

    private StoredFileInfo toInfo(StoredFile file) {
        return StoredFileInfo.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploaderId(file.getUploaderId())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
