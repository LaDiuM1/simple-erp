package io.github.ladium1.erp.global.storage.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 업로드 — 메뉴 권한 없이 인증만 요구 (모든 도메인의 첨부가 공용 사용).
 * <p>
 * 다운로드는 fileId 만으로 타인 파일에 접근할 수 없도록 소유 도메인이 접근 통제와 함께
 * 제공한다 (예: 결재 첨부는 ApprovalController, 드라이브는 DriveController).
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private static final String IS_AUTHENTICATED = "isAuthenticated()";

    private final FileStorageService fileStorageService;
    private final DataScopeContextProvider dataScopeContextProvider;

    @PostMapping
    @PreAuthorize(IS_AUTHENTICATED)
    public StoredFileInfo upload(@RequestPart("file") MultipartFile file) {
        Long uploaderId = dataScopeContextProvider.current().employeeId();
        return fileStorageService.store(file.getOriginalFilename(), file.getContentType(), readBytes(file), uploaderId);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(StorageErrorCode.STORAGE_IO_FAILED);
        }
    }
}
