package io.github.ladium1.erp.drive.internal.service;

import io.github.ladium1.erp.drive.internal.dto.DriveBrowseResponse;
import io.github.ladium1.erp.drive.internal.dto.DriveFileDownload;
import io.github.ladium1.erp.drive.internal.dto.FolderCreateRequest;
import io.github.ladium1.erp.drive.internal.entity.DriveFile;
import io.github.ladium1.erp.drive.internal.entity.DriveFolder;
import io.github.ladium1.erp.drive.internal.exception.DriveErrorCode;
import io.github.ladium1.erp.drive.internal.repository.DriveFileRepository;
import io.github.ladium1.erp.drive.internal.repository.DriveFolderRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DriveServiceTest {

    @InjectMocks
    private DriveService driveService;

    @Mock private DriveFolderRepository driveFolderRepository;
    @Mock private DriveFileRepository driveFileRepository;
    @Mock private FileStorageApi fileStorageApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private DataScopeContextProvider dataScopeContextProvider;

    private final Long ME = 7L;

    private DriveFolder folder(Long id, String name, Long parentId) {
        DriveFolder folder = DriveFolder.builder().name(name).parentId(parentId).createdBy(ME).build();
        ReflectionTestUtils.setField(folder, "id", id);
        return folder;
    }

    private DriveFile file(Long id, DriveFolder folder, Long storageFileId, String name) {
        DriveFile file = DriveFile.builder()
                .folder(folder).storageFileId(storageFileId).name(name).uploaderId(ME)
                .build();
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }

    private void mockCurrentUser() {
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(ME, null, Set.of()));
    }

    @Test
    @DisplayName("browse — breadcrumb 은 루트 → 현재 순서")
    void browse_breadcrumb_chain() {
        // given
        DriveFolder current = folder(3L, "현재폴더", 2L);
        given(driveFolderRepository.findById(1L)).willReturn(Optional.of(folder(1L, "루트폴더", null)));
        given(driveFolderRepository.findById(2L)).willReturn(Optional.of(folder(2L, "중간폴더", 1L)));
        given(driveFolderRepository.findById(3L)).willReturn(Optional.of(current));
        given(driveFolderRepository.findAllByParentId(3L)).willReturn(List.of());
        given(driveFileRepository.findAllByFolderId(3L)).willReturn(List.of(file(10L, current, 100L, "회의록.pdf")));
        given(fileStorageApi.getInfos(Map.of(100L, FileOwner.driveFile(10L)))).willReturn(Map.of(
                100L, StoredFileInfo.builder()
                        .id(100L).originalName("회의록.pdf").size(2048L).uploaderId(ME).build()
        ));
        given(employeeApi.findByIds(List.of(ME))).willReturn(List.of(
                EmployeeInfo.builder().id(ME).name("홍길동").build()
        ));

        // when
        DriveBrowseResponse response = driveService.browse(3L);

        // then
        assertThat(response.breadcrumb())
                .extracting(DriveBrowseResponse.BreadcrumbItem::id)
                .containsExactly(1L, 2L, 3L);
        assertThat(response.breadcrumb())
                .extracting(DriveBrowseResponse.BreadcrumbItem::name)
                .containsExactly("루트폴더", "중간폴더", "현재폴더");
        assertThat(response.files()).hasSize(1);
        assertThat(response.files().getFirst().size()).isEqualTo(2048L);
        assertThat(response.files().getFirst().uploaderName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("폴더 생성 성공")
    void create_folder_success() {
        // given
        mockCurrentUser();
        given(driveFolderRepository.existsById(1L)).willReturn(true);
        given(driveFolderRepository.existsByParentIdAndName(1L, "자료실")).willReturn(false);

        DriveFolder saved = folder(10L, "자료실", 1L);
        given(driveFolderRepository.save(any(DriveFolder.class))).willReturn(saved);

        // when
        Long id = driveService.createFolder(new FolderCreateRequest("자료실", 1L));

        // then
        assertThat(id).isEqualTo(10L);

        ArgumentCaptor<DriveFolder> captor = ArgumentCaptor.forClass(DriveFolder.class);
        verify(driveFolderRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("자료실");
        assertThat(captor.getValue().getParentId()).isEqualTo(1L);
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(ME);
    }

    @Test
    @DisplayName("같은 부모 안 폴더명 중복 시 409")
    void create_folder_fail_duplicate_name() {
        // given
        given(driveFolderRepository.existsById(1L)).willReturn(true);
        given(driveFolderRepository.existsByParentIdAndName(1L, "자료실")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> driveService.createFolder(new FolderCreateRequest("자료실", 1L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DriveErrorCode.DUPLICATE_FOLDER_NAME);
        verify(driveFolderRepository, never()).save(any());
    }

    @Test
    @DisplayName("빈 폴더 삭제 성공")
    void delete_folder_success() {
        // given
        given(driveFolderRepository.existsById(5L)).willReturn(true);
        given(driveFolderRepository.existsByParentId(5L)).willReturn(false);
        given(driveFileRepository.existsByFolderId(5L)).willReturn(false);

        // when
        driveService.deleteFolder(5L);

        // then
        verify(driveFolderRepository).deleteById(5L);
    }

    @Test
    @DisplayName("하위 폴더가 있는 폴더 삭제 시 409")
    void delete_folder_fail_not_empty() {
        // given
        given(driveFolderRepository.existsById(5L)).willReturn(true);
        given(driveFolderRepository.existsByParentId(5L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> driveService.deleteFolder(5L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DriveErrorCode.FOLDER_NOT_EMPTY);
        verify(driveFolderRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("파일이 있는 폴더 삭제 시 409")
    void delete_folder_fail_contains_files() {
        // given
        given(driveFolderRepository.existsById(5L)).willReturn(true);
        given(driveFolderRepository.existsByParentId(5L)).willReturn(false);
        given(driveFileRepository.existsByFolderId(5L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> driveService.deleteFolder(5L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DriveErrorCode.FOLDER_NOT_EMPTY);
        verify(driveFolderRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("파일 업로드 — storage 저장 + DriveFile 생성")
    void upload_file_success() {
        // given
        mockCurrentUser();
        byte[] content = "file-content".getBytes();
        DriveFolder parent = folder(1L, "자료실", null);
        given(driveFolderRepository.findById(1L)).willReturn(Optional.of(parent));
        given(fileStorageApi.store("보고서.xlsx", "application/octet-stream", content, ME)).willReturn(
                StoredFileInfo.builder().id(100L).originalName("보고서.xlsx").size(content.length).uploaderId(ME).build()
        );

        DriveFile saved = file(10L, parent, 100L, "보고서.xlsx");
        given(driveFileRepository.save(any(DriveFile.class))).willReturn(saved);

        // when
        Long id = driveService.uploadFile(1L, "보고서.xlsx", "application/octet-stream", content);

        // then
        assertThat(id).isEqualTo(10L);

        ArgumentCaptor<DriveFile> captor = ArgumentCaptor.forClass(DriveFile.class);
        var order = inOrder(fileStorageApi, driveFileRepository);
        order.verify(fileStorageApi).store("보고서.xlsx", "application/octet-stream", content, ME);
        order.verify(driveFileRepository).save(captor.capture());
        order.verify(fileStorageApi).claim(List.of(100L), FileOwner.driveFile(10L), ME);
        assertThat(captor.getValue().getFolder().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getStorageFileId()).isEqualTo(100L);
        assertThat(captor.getValue().getName()).isEqualTo("보고서.xlsx");
        assertThat(captor.getValue().getUploaderId()).isEqualTo(ME);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " \t"})
    @DisplayName("파일명이 비어 있으면 storage가 정규화한 이름으로 DriveFile 생성")
    void upload_file_uses_storage_normalized_original_name(String originalName) {
        mockCurrentUser();
        byte[] content = "file-content".getBytes();
        given(fileStorageApi.store(originalName, null, content, ME)).willReturn(
                StoredFileInfo.builder()
                        .id(100L)
                        .originalName("upload.bin")
                        .size(content.length)
                        .uploaderId(ME)
                        .build()
        );
        given(driveFileRepository.save(any(DriveFile.class)))
                .willReturn(file(10L, null, 100L, "upload.bin"));

        Long id = driveService.uploadFile(null, originalName, null, content);

        assertThat(id).isEqualTo(10L);
        ArgumentCaptor<DriveFile> captor = ArgumentCaptor.forClass(DriveFile.class);
        verify(driveFileRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("upload.bin");
        verify(fileStorageApi).claim(List.of(100L), FileOwner.driveFile(10L), ME);
    }

    @Test
    @DisplayName("존재하지 않는 폴더에 업로드 시 404")
    void upload_file_fail_folder_not_found() {
        // given
        given(driveFolderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> driveService.uploadFile(1L, "보고서.xlsx", null, "file-content".getBytes()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DriveErrorCode.FOLDER_NOT_FOUND);
        verify(fileStorageApi, never()).store(any(), any(), any(), any());
    }

    @Test
    @DisplayName("인증 직원 정보 없는 폴더 생성 시 403")
    void create_folder_fail_unauthenticated() {
        // given
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());
        given(driveFolderRepository.existsById(1L)).willReturn(true);
        given(driveFolderRepository.existsByParentIdAndName(1L, "자료실")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> driveService.createFolder(new FolderCreateRequest("자료실", 1L)))
                .isInstanceOf(AccessDeniedException.class);
        verify(driveFolderRepository, never()).save(any());
    }

    @Test
    @DisplayName("파일 다운로드는 DriveFile 소유권으로 메타데이터와 본체 조회")
    void download_file_uses_expected_owner() {
        DriveFile target = file(10L, null, 100L, "보고서.xlsx");
        FileOwner owner = FileOwner.driveFile(10L);
        given(driveFileRepository.findById(10L)).willReturn(Optional.of(target));
        given(fileStorageApi.getInfo(100L, owner)).willReturn(
                StoredFileInfo.builder().id(100L).contentType("application/octet-stream").build());
        given(fileStorageApi.loadContent(100L, owner)).willReturn("content".getBytes());

        DriveFileDownload download = driveService.downloadFile(10L);

        assertThat(download.name()).isEqualTo("보고서.xlsx");
        assertThat(download.content()).isEqualTo("content".getBytes());
    }

    @Test
    @DisplayName("파일 삭제 — 소유권 확인 뒤 DriveFile 제거")
    void delete_file_success() {
        // given
        DriveFile target = file(10L, folder(1L, "자료실", null), 100L, "보고서.xlsx");
        given(driveFileRepository.findById(10L)).willReturn(Optional.of(target));

        // when
        driveService.deleteFile(10L);

        // then
        var order = inOrder(fileStorageApi, driveFileRepository);
        order.verify(fileStorageApi).requestDeletion(List.of(100L), FileOwner.driveFile(10L));
        order.verify(driveFileRepository).delete(target);
    }
}
