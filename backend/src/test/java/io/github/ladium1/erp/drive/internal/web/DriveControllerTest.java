package io.github.ladium1.erp.drive.internal.web;

import io.github.ladium1.erp.drive.internal.dto.DriveBrowseResponse;
import io.github.ladium1.erp.drive.internal.dto.DriveFileDownload;
import io.github.ladium1.erp.drive.internal.dto.FolderCreateRequest;
import io.github.ladium1.erp.drive.internal.dto.FolderRenameRequest;
import io.github.ladium1.erp.drive.internal.exception.DriveErrorCode;
import io.github.ladium1.erp.drive.internal.service.DriveService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriveController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DriveService driveService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("드라이브 탐색 성공 — breadcrumb + 폴더 + 파일")
    void browse_success() throws Exception {
        // given
        DriveBrowseResponse response = new DriveBrowseResponse(
                List.of(new DriveBrowseResponse.BreadcrumbItem(1L, "루트폴더")),
                List.of(new DriveBrowseResponse.FolderItem(2L, "자료실", null)),
                List.of(new DriveBrowseResponse.FileItem(10L, "회의록.pdf", 2048L, 7L, "홍길동", null))
        );
        given(driveService.browse(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/drive").param("folderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.breadcrumb[0].name").value("루트폴더"))
                .andExpect(jsonPath("$.data.folders[0].name").value("자료실"))
                .andExpect(jsonPath("$.data.files[0].size").value(2048))
                .andExpect(jsonPath("$.data.files[0].uploaderName").value("홍길동"));
    }

    @Test
    @DisplayName("루트 탐색 — folderId 미지정은 null 위임")
    void browse_root_success() throws Exception {
        // given
        given(driveService.browse(null)).willReturn(
                new DriveBrowseResponse(List.of(), List.of(), List.of())
        );

        // when & then
        mockMvc.perform(get("/api/v1/drive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.breadcrumb").isEmpty());
        verify(driveService).browse(null);
    }

    @Test
    @DisplayName("폴더 생성 성공")
    void create_folder_success() throws Exception {
        // given
        FolderCreateRequest request = new FolderCreateRequest("자료실", 1L);
        given(driveService.createFolder(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/drive/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("이름 없는 폴더 생성 시 400")
    void create_folder_fail_blank_name() throws Exception {
        // given
        FolderCreateRequest request = new FolderCreateRequest("", 1L);

        // when & then
        mockMvc.perform(post("/api/v1/drive/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(driveService, never()).createFolder(any());
    }

    @Test
    @DisplayName("중복 폴더명 생성 시 409")
    void create_folder_fail_duplicate_name() throws Exception {
        // given
        FolderCreateRequest request = new FolderCreateRequest("자료실", 1L);
        willThrow(new BusinessException(DriveErrorCode.DUPLICATE_FOLDER_NAME))
                .given(driveService).createFolder(any());

        // when & then
        mockMvc.perform(post("/api/v1/drive/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("폴더 이름 변경 성공")
    void rename_folder_success() throws Exception {
        // given
        FolderRenameRequest request = new FolderRenameRequest("새이름");

        // when & then
        mockMvc.perform(put("/api/v1/drive/folders/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(driveService).renameFolder(eq(5L), any());
    }

    @Test
    @DisplayName("폴더 삭제 성공")
    void delete_folder_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/drive/folders/{id}", 5L))
                .andExpect(status().isNoContent());
        verify(driveService).deleteFolder(5L);
    }

    @Test
    @DisplayName("비어 있지 않은 폴더 삭제 시 409")
    void delete_folder_fail_not_empty() throws Exception {
        // given
        willThrow(new BusinessException(DriveErrorCode.FOLDER_NOT_EMPTY))
                .given(driveService).deleteFolder(5L);

        // when & then
        mockMvc.perform(delete("/api/v1/drive/folders/{id}", 5L))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void upload_file_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "보고서.xlsx", "application/vnd.ms-excel", "file-content".getBytes()
        );
        given(driveService.uploadFile(eq(1L), eq("보고서.xlsx"), eq("application/vnd.ms-excel"), any()))
                .willReturn(10L);

        // when & then
        mockMvc.perform(multipart("/api/v1/drive/files")
                        .file(file)
                        .param("folderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("파일 다운로드 성공 — 바이트 + 첨부 헤더")
    void download_file_success() throws Exception {
        // given
        byte[] content = "pdf-bytes".getBytes();
        given(driveService.downloadFile(10L))
                .willReturn(new DriveFileDownload("report.pdf", "application/pdf", content));

        // when & then
        mockMvc.perform(get("/api/v1/drive/files/{id}/download", 10L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("contentType 없는 다운로드 — octet-stream 폴백")
    void download_file_null_content_type_falls_back() throws Exception {
        // given
        byte[] content = "raw-bytes".getBytes();
        given(driveService.downloadFile(10L))
                .willReturn(new DriveFileDownload("data.bin", null, content));

        // when & then
        mockMvc.perform(get("/api/v1/drive/files/{id}/download", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("파싱 불가 contentType 다운로드 — octet-stream 폴백")
    void download_file_invalid_content_type_falls_back() throws Exception {
        // given
        byte[] content = "raw-bytes".getBytes();
        given(driveService.downloadFile(10L))
                .willReturn(new DriveFileDownload("data.bin", "잘못된 타입", content));

        // when & then
        mockMvc.perform(get("/api/v1/drive/files/{id}/download", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 404")
    void download_file_fail_not_found() throws Exception {
        // given
        given(driveService.downloadFile(99L))
                .willThrow(new BusinessException(DriveErrorCode.FILE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/drive/files/{id}/download", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void delete_file_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/drive/files/{id}", 10L))
                .andExpect(status().isNoContent());
        verify(driveService).deleteFile(10L);
    }
}
