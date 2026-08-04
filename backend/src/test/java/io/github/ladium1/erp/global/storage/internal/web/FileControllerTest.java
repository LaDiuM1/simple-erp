package io.github.ladium1.erp.global.storage.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import io.github.ladium1.erp.global.storage.internal.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private DataScopeContextProvider dataScopeContextProvider;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    private StoredFileInfo storedFileInfo(String contentType) {
        return StoredFileInfo.builder()
                .id(100L)
                .originalName("hello.txt")
                .contentType(contentType)
                .size(10L)
                .uploaderId(1L)
                .build();
    }

    @Test
    @DisplayName("파일 업로드 성공 — 업로더 직원 ID 연결")
    void upload_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello file".getBytes()
        );
        given(dataScopeContextProvider.current()).willReturn(new DataScopeContext(1L, null, Set.of()));
        given(fileStorageService.store(eq("hello.txt"), eq("text/plain"), any(), eq(1L)))
                .willReturn(storedFileInfo("text/plain"));

        // when & then
        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.originalName").value("hello.txt"))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 400")
    void upload_fail_empty_file() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );
        given(dataScopeContextProvider.current()).willReturn(new DataScopeContext(1L, null, Set.of()));
        willThrow(new BusinessException(StorageErrorCode.EMPTY_FILE))
                .given(fileStorageService).store(eq("empty.txt"), eq("text/plain"), any(), eq(1L));

        // when & then
        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증 직원 식별자가 없으면 업로드 거부")
    void upload_rejects_missing_employee_identity() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello file".getBytes());
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isForbidden());
        verify(fileStorageService, never()).store(any(), any(), any(), any());
    }
}
