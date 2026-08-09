package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFileStatus;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.repository.StoredFileRepository;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FileStorageService implements FileStorageApi {

    private static final String FALLBACK_ORIGINAL_NAME = "upload.bin";
    private static final int CLEANUP_BATCH_SIZE = 100;

    private final StoredFileRepository storedFileRepository;
    private final DemoProtectionPolicy demoProtectionPolicy;
    private final Path basePath;
    private final ContentWriter contentWriter;

    @Autowired
    public FileStorageService(
            StoredFileRepository storedFileRepository,
            DemoProtectionPolicy demoProtectionPolicy,
            @Value("${erp.storage.local.base-path}") String basePath
    ) {
        this(storedFileRepository, demoProtectionPolicy, basePath, FileStorageService::writeAtomically);
    }

    FileStorageService(
            StoredFileRepository storedFileRepository,
            DemoProtectionPolicy demoProtectionPolicy,
            String basePath,
            ContentWriter contentWriter
    ) {
        this.storedFileRepository = storedFileRepository;
        this.demoProtectionPolicy = demoProtectionPolicy;
        this.basePath = Path.of(basePath);
        this.contentWriter = contentWriter;
    }

    @Override
    @Transactional
    public StoredFileInfo store(String originalName, String contentType, byte[] content, Long uploaderId) {
        demoProtectionPolicy.assertUploadAllowed();
        if (content == null || content.length == 0) {
            throw new BusinessException(StorageErrorCode.EMPTY_FILE);
        }

        StoredFile file = StoredFile.builder()
                .originalName(normalizeOriginalName(originalName))
                .storedName(UUID.randomUUID().toString())
                .contentType(normalizeContentType(contentType))
                .size(content.length)
                .uploaderId(uploaderId)
                .build();
        StoredFile saved = storedFileRepository.save(file);

        // 저장 실패 (STORAGE_IO_FAILED) 시 런타임 예외로 메타 INSERT 도 함께 롤백
        writeContent(saved, content);
        return toInfo(saved);
    }

    @Override
    public StoredFileInfo getInfo(Long fileId, FileOwner expectedOwner) {
        return toInfo(getClaimedStoredFile(fileId, expectedOwner));
    }

    @Override
    public List<StoredFileInfo> getInfos(List<Long> fileIds, FileOwner expectedOwner) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        if (expectedOwner == null || fileIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(StorageErrorCode.FILE_NOT_FOUND);
        }
        Map<Long, FileOwner> expectedOwners = fileIds.stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), ignored -> expectedOwner));
        Map<Long, StoredFileInfo> infos = getInfos(expectedOwners);
        return fileIds.stream()
                .map(infos::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Map<Long, StoredFileInfo> getInfos(Map<Long, FileOwner> expectedOwners) {
        if (expectedOwners == null || expectedOwners.isEmpty()) {
            return Map.of();
        }
        if (expectedOwners.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
            throw new BusinessException(StorageErrorCode.FILE_NOT_FOUND);
        }

        Map<Long, StoredFile> filesById = storedFileRepository.findAllById(expectedOwners.keySet()).stream()
                .collect(Collectors.toMap(StoredFile::getId, Function.identity()));
        Map<Long, StoredFileInfo> result = new LinkedHashMap<>();
        expectedOwners.forEach((fileId, expectedOwner) -> {
            StoredFile file = filesById.get(fileId);
            if (file == null || !file.isClaimedBy(expectedOwner)) {
                throw new BusinessException(StorageErrorCode.FILE_NOT_FOUND);
            }
            result.put(fileId, toInfo(file));
        });
        return Map.copyOf(result);
    }

    @Override
    public byte[] loadContent(Long fileId, FileOwner expectedOwner) {
        StoredFile file = getClaimedStoredFile(fileId, expectedOwner);
        try {
            return Files.readAllBytes(resolveContentPath(file));
        } catch (IOException e) {
            throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
        }
    }

    @Override
    @Transactional
    public void claim(List<Long> fileIds, FileOwner owner, Long uploaderId) {
        List<Long> validatedIds = validateClaimFileIds(fileIds);
        if (validatedIds.isEmpty()) {
            return;
        }
        if (owner == null || uploaderId == null) {
            throw new BusinessException(StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);
        }

        List<StoredFile> files = getStoredFilesForUpdate(validatedIds, StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);
        if (files.stream().anyMatch(file -> !file.canBeClaimedBy(owner, uploaderId))) {
            throw new BusinessException(StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);
        }
        files.forEach(file -> file.claim(owner));
    }

    @Override
    @Transactional
    public void requestDeletion(List<Long> fileIds, FileOwner owner) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        List<Long> sortedIds = fileIds.stream().filter(Objects::nonNull).distinct().sorted().toList();
        if (owner == null || sortedIds.size() != fileIds.size()) {
            throw new BusinessException(StorageErrorCode.FILE_NOT_FOUND);
        }

        List<StoredFile> files = getStoredFilesForUpdate(sortedIds, StorageErrorCode.FILE_NOT_FOUND);
        if (files.stream().anyMatch(file -> !file.canRequestDeletionBy(owner))) {
            throw new BusinessException(StorageErrorCode.FILE_NOT_FOUND);
        }
        files.forEach(StoredFile::requestDeletion);
    }

    @Transactional
    public int markExpiredPendingForDeletion(LocalDateTime createdBefore) {
        List<StoredFile> expired = storedFileRepository.findCreatedBeforeForUpdate(
                StoredFileStatus.PENDING,
                createdBefore,
                PageRequest.of(0, CLEANUP_BATCH_SIZE)
        );
        expired.stream()
                .filter(StoredFile::isPending)
                .forEach(StoredFile::requestDeletion);
        return expired.size();
    }

    @Transactional
    public int deletePendingFiles() {
        if (demoProtectionPolicy.shouldRetainStoredFiles()) {
            return 0;
        }
        List<StoredFile> files = storedFileRepository.findByStatusForUpdate(
                StoredFileStatus.DELETE_PENDING,
                PageRequest.of(0, CLEANUP_BATCH_SIZE)
        );
        for (StoredFile file : files) {
            try {
                Files.deleteIfExists(resolveContentPath(file));
            } catch (IOException e) {
                throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
            }
        }
        storedFileRepository.deleteAll(files);
        return files.size();
    }

    private StoredFile getClaimedStoredFile(Long fileId, FileOwner expectedOwner) {
        return storedFileRepository.findById(fileId)
                .filter(file -> file.isClaimedBy(expectedOwner))
                .orElseThrow(() -> new BusinessException(StorageErrorCode.FILE_NOT_FOUND));
    }

    private List<StoredFile> getStoredFilesForUpdate(List<Long> fileIds, StorageErrorCode errorCode) {
        List<StoredFile> files = storedFileRepository.findAllByIdForUpdate(fileIds);
        if (files.size() != fileIds.size()) {
            throw new BusinessException(errorCode);
        }
        return files;
    }

    private static List<Long> validateClaimFileIds(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        if (fileIds.size() > RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE
                || fileIds.stream().anyMatch(Objects::isNull)
                || fileIds.stream().distinct().count() != fileIds.size()) {
            throw new BusinessException(StorageErrorCode.INVALID_FILE_REFERENCES);
        }
        return fileIds.stream().sorted().toList();
    }

    private void writeContent(StoredFile file, byte[] content) {
        Path path = resolveContentPath(file);
        try {
            Files.createDirectories(path.getParent());
            contentWriter.write(path, content);
            registerRollbackCleanup(path);
        } catch (IOException e) {
            deleteAfterFailedWrite(path, e);
            throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
        } catch (RuntimeException e) {
            deleteAfterFailedWrite(path, e);
            throw e;
        }
    }

    private void registerRollbackCleanup(Path path) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    return;
                }
                try {
                    Files.deleteIfExists(path);
                } catch (IOException cleanupFailure) {
                    log.error("롤백된 파일 본체를 정리하지 못했습니다: path={}", path, cleanupFailure);
                }
            }
        });
    }

    private static void deleteAfterFailedWrite(Path path, Exception failure) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private static void writeAtomically(Path target, byte[] content) throws IOException {
        Path parent = target.getParent();
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("저장 대상 파일이 이미 존재합니다: " + target.getFileName());
        }

        Path temporary = Files.createTempFile(parent, ".upload-", ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, content, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target);
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
                Files.deleteIfExists(target);
            }
        }
    }

    private static String normalizeOriginalName(String originalName) {
        return StringUtils.hasText(originalName) ? originalName.trim() : FALLBACK_ORIGINAL_NAME;
    }

    private static String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType)
                ? contentType.trim()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
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

    @FunctionalInterface
    interface ContentWriter {
        void write(Path target, byte[] content) throws IOException;
    }
}
