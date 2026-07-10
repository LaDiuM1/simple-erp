package io.github.ladium1.erp.drive.internal.web;

import io.github.ladium1.erp.drive.internal.dto.DriveBrowseResponse;
import io.github.ladium1.erp.drive.internal.dto.DriveFileDownload;
import io.github.ladium1.erp.drive.internal.dto.FolderCreateRequest;
import io.github.ladium1.erp.drive.internal.dto.FolderRenameRequest;
import io.github.ladium1.erp.drive.internal.exception.DriveErrorCode;
import io.github.ladium1.erp.drive.internal.service.DriveService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.DownloadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/drive")
@RequiredArgsConstructor
public class DriveController {

    private static final String MENU_CODE = "DRIVE";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final DriveService driveService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public DriveBrowseResponse browse(@RequestParam(required = false) Long folderId) {
        return driveService.browse(folderId);
    }

    @PostMapping("/folders")
    @PreAuthorize(CAN_WRITE)
    public Long createFolder(@Valid @RequestBody FolderCreateRequest request) {
        return driveService.createFolder(request);
    }

    @PutMapping("/folders/{id}")
    @PreAuthorize(CAN_WRITE)
    public void renameFolder(@PathVariable Long id, @Valid @RequestBody FolderRenameRequest request) {
        driveService.renameFolder(id, request);
    }

    @DeleteMapping("/folders/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteFolder(@PathVariable Long id) {
        driveService.deleteFolder(id);
    }

    @PostMapping("/files")
    @PreAuthorize(CAN_WRITE)
    public Long uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long folderId
    ) {
        return driveService.uploadFile(folderId, file.getOriginalFilename(), file.getContentType(), readContent(file));
    }

    @GetMapping("/files/{id}/download")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        DriveFileDownload download = driveService.downloadFile(id);
        return DownloadResponse.attachment(download.content(), download.name(), download.contentType());
    }

    @DeleteMapping("/files/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteFile(@PathVariable Long id) {
        driveService.deleteFile(id);
    }

    /**
     * 멀티파트 본체 read 실패를 도메인 에러로 변환 — 컨트롤러 시그니처에서 checked IOException 제거.
     */
    private byte[] readContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(DriveErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
