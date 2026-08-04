package io.github.ladium1.erp.drive.internal.service;

import io.github.ladium1.erp.drive.internal.dto.DriveBrowseResponse;
import io.github.ladium1.erp.drive.internal.dto.DriveFileDownload;
import io.github.ladium1.erp.drive.internal.dto.FolderCreateRequest;
import io.github.ladium1.erp.drive.internal.dto.FolderRenameRequest;
import io.github.ladium1.erp.drive.internal.entity.DriveFile;
import io.github.ladium1.erp.drive.internal.entity.DriveFolder;
import io.github.ladium1.erp.drive.internal.exception.DriveErrorCode;
import io.github.ladium1.erp.drive.internal.repository.DriveFileRepository;
import io.github.ladium1.erp.drive.internal.repository.DriveFolderRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전사 공유 드라이브 — 폴더 트리 + 파일 배치만 소유하고, 파일 본체 / 메타는 storage 모듈에 위임.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriveService {

    private final DriveFolderRepository driveFolderRepository;
    private final DriveFileRepository driveFileRepository;
    private final FileStorageApi fileStorageApi;
    private final EmployeeApi employeeApi;
    private final DataScopeContextProvider dataScopeContextProvider;

    public DriveBrowseResponse browse(Long folderId) {
        List<DriveBrowseResponse.BreadcrumbItem> breadcrumb = buildBreadcrumb(folderId);

        List<DriveFolder> folders = folderId == null
                ? driveFolderRepository.findAllByParentIdIsNull()
                : driveFolderRepository.findAllByParentId(folderId);
        List<DriveFile> files = folderId == null
                ? driveFileRepository.findAllByFolderIsNull()
                : driveFileRepository.findAllByFolderId(folderId);

        return new DriveBrowseResponse(breadcrumb, toFolderItems(folders), toFileItems(files));
    }

    @Auditable(menu = Menu.DRIVE, action = AuditAction.CREATE, targetType = "DriveFolder", targetIdFromReturn = true)
    @Transactional
    public Long createFolder(FolderCreateRequest request) {
        if (request.parentId() != null && !driveFolderRepository.existsById(request.parentId())) {
            throw new BusinessException(DriveErrorCode.FOLDER_NOT_FOUND);
        }
        String name = request.name().trim();
        if (folderNameExists(request.parentId(), name)) {
            throw new BusinessException(DriveErrorCode.DUPLICATE_FOLDER_NAME);
        }

        DriveFolder folder = DriveFolder.builder()
                .name(name)
                .parentId(request.parentId())
                .createdBy(currentEmployeeId())
                .build();

        return driveFolderRepository.save(folder).getId();
    }

    @Auditable(menu = Menu.DRIVE, action = AuditAction.UPDATE, targetType = "DriveFolder", targetIdParam = "id")
    @Transactional
    public void renameFolder(Long id, FolderRenameRequest request) {
        DriveFolder folder = getFolder(id);

        String name = request.name().trim();
        if (!name.equals(folder.getName()) && folderNameExists(folder.getParentId(), name)) {
            throw new BusinessException(DriveErrorCode.DUPLICATE_FOLDER_NAME);
        }

        folder.rename(name);
    }

    @Auditable(menu = Menu.DRIVE, action = AuditAction.DELETE, targetType = "DriveFolder", targetIdParam = "id")
    @Transactional
    public void deleteFolder(Long id) {
        if (!driveFolderRepository.existsById(id)) {
            throw new BusinessException(DriveErrorCode.FOLDER_NOT_FOUND);
        }
        if (driveFolderRepository.existsByParentId(id) || driveFileRepository.existsByFolderId(id)) {
            throw new BusinessException(DriveErrorCode.FOLDER_NOT_EMPTY);
        }
        driveFolderRepository.deleteById(id);
    }

    @Auditable(menu = Menu.DRIVE, action = AuditAction.CREATE, targetType = "DriveFile", targetIdFromReturn = true)
    @Transactional
    public Long uploadFile(Long folderId, String originalName, String contentType, byte[] content) {
        DriveFolder folder = folderId == null ? null : getFolder(folderId);

        Long uploaderId = currentEmployeeId();
        StoredFileInfo stored = fileStorageApi.store(originalName, contentType, content, uploaderId);

        DriveFile file = DriveFile.builder()
                .folder(folder)
                .storageFileId(stored.id())
                .name(stored.originalName())
                .uploaderId(uploaderId)
                .build();

        Long driveFileId = driveFileRepository.save(file).getId();
        fileStorageApi.claim(List.of(stored.id()), FileOwner.driveFile(driveFileId), uploaderId);
        return driveFileId;
    }

    public DriveFileDownload downloadFile(Long id) {
        DriveFile file = getFile(id);
        FileOwner owner = FileOwner.driveFile(id);
        StoredFileInfo info = fileStorageApi.getInfo(file.getStorageFileId(), owner);
        return new DriveFileDownload(file.getName(), info.contentType(),
                fileStorageApi.loadContent(file.getStorageFileId(), owner));
    }

    @Auditable(menu = Menu.DRIVE, action = AuditAction.DELETE, targetType = "DriveFile", targetIdParam = "id")
    @Transactional
    public void deleteFile(Long id) {
        DriveFile file = getFile(id);
        fileStorageApi.requestDeletion(List.of(file.getStorageFileId()), FileOwner.driveFile(id));
        driveFileRepository.delete(file);
    }

    /**
     * parentId 체인을 루트까지 따라 올라간 뒤 루트 → 현재 순서로 반환 (자기 자신 포함).
     */
    private List<DriveBrowseResponse.BreadcrumbItem> buildBreadcrumb(Long folderId) {
        if (folderId == null) {
            return List.of();
        }
        Deque<DriveBrowseResponse.BreadcrumbItem> chain = new ArrayDeque<>();
        Long currentId = folderId;
        while (currentId != null) {
            DriveFolder folder = getFolder(currentId);
            chain.addFirst(new DriveBrowseResponse.BreadcrumbItem(folder.getId(), folder.getName()));
            currentId = folder.getParentId();
        }
        return List.copyOf(chain);
    }

    private List<DriveBrowseResponse.FolderItem> toFolderItems(List<DriveFolder> folders) {
        return folders.stream()
                .map(f -> new DriveBrowseResponse.FolderItem(f.getId(), f.getName(), f.getCreatedAt()))
                .toList();
    }

    private List<DriveBrowseResponse.FileItem> toFileItems(List<DriveFile> files) {
        Map<Long, FileOwner> expectedOwners = files.stream().collect(Collectors.toMap(
                DriveFile::getStorageFileId,
                file -> FileOwner.driveFile(file.getId())
        ));
        Map<Long, StoredFileInfo> storageInfos = fileStorageApi.getInfos(expectedOwners);
        Map<Long, String> uploaderNames = employeeApi
                .findByIds(files.stream().map(DriveFile::getUploaderId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(EmployeeInfo::id, EmployeeInfo::name));

        return files.stream()
                .map(f -> {
                    StoredFileInfo info = storageInfos.get(f.getStorageFileId());
                    return new DriveBrowseResponse.FileItem(
                            f.getId(),
                            f.getName(),
                            info == null ? 0L : info.size(),
                            f.getUploaderId(),
                            uploaderNames.get(f.getUploaderId()),
                            f.getCreatedAt()
                    );
                })
                .toList();
    }

    private boolean folderNameExists(Long parentId, String name) {
        return parentId == null
                ? driveFolderRepository.existsByParentIdIsNullAndName(name)
                : driveFolderRepository.existsByParentIdAndName(parentId, name);
    }

    private DriveFolder getFolder(Long id) {
        return driveFolderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DriveErrorCode.FOLDER_NOT_FOUND));
    }

    private DriveFile getFile(Long id) {
        return driveFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DriveErrorCode.FILE_NOT_FOUND));
    }

    private Long currentEmployeeId() {
        Long employeeId = dataScopeContextProvider.current().employeeId();
        if (employeeId == null) {
            throw new AccessDeniedException("인증된 직원 정보를 찾을 수 없습니다.");
        }
        return employeeId;
    }
}
