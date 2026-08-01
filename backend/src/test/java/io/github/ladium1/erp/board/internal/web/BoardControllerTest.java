package io.github.ladium1.erp.board.internal.web;

import io.github.ladium1.erp.board.internal.dto.CommentCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostAttachmentDownload;
import io.github.ladium1.erp.board.internal.dto.PostCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostDetailResponse;
import io.github.ladium1.erp.board.internal.dto.PostSummaryResponse;
import io.github.ladium1.erp.board.internal.dto.PostUpdateRequest;
import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import io.github.ladium1.erp.board.internal.exception.BoardErrorCode;
import io.github.ladium1.erp.board.internal.service.BoardService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void search_success() throws Exception {
        // given
        PostSummaryResponse summary = new PostSummaryResponse(
                1L, BoardCategory.NOTICE, "휴무 안내", "홍길동", 3L, null
        );
        PageResponse<PostSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(boardService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("휴무 안내"))
                .andExpect(jsonPath("$.data.content[0].commentCount").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        PostDetailResponse detail = new PostDetailResponse(
                7L, BoardCategory.FREE, "제목", "본문", 1L, "홍길동",
                null, List.of(), List.of()
        );
        given(boardService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/boards/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.authorName").value("홍길동"));
    }

    @Test
    @DisplayName("첨부 다운로드 성공 — 바이트 + 첨부 헤더")
    void download_attachment_success() throws Exception {
        // given
        byte[] content = "pdf-bytes".getBytes();
        given(boardService.downloadAttachment(7L, 10L))
                .willReturn(new PostAttachmentDownload("report.pdf", "application/pdf", content));

        // when & then
        mockMvc.perform(get("/api/v1/boards/{id}/attachments/{fileId}", 7L, 10L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("연결되지 않은 첨부 다운로드 시 404")
    void download_attachment_fail_not_found() throws Exception {
        // given
        given(boardService.downloadAttachment(7L, 999L))
                .willThrow(new BusinessException(BoardErrorCode.POST_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/boards/{id}/attachments/{fileId}", 7L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(boardService.getDetail(99L))
                .willThrow(new BusinessException(BoardErrorCode.POST_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/boards/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void create_success() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(BoardCategory.FREE, "제목", "본문", List.of(10L));
        given(boardService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("한글 본문은 4,000자까지 등록 허용")
    void create_accepts_max_korean_content() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(
                BoardCategory.FREE, "제목", "가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH), null);
        given(boardService.create(any())).willReturn(43L);

        // when & then
        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(43));
        verify(boardService).create(any());
    }

    @Test
    @DisplayName("한글 본문이 4,000자를 넘으면 등록 서비스 호출 전에 400")
    void create_rejects_korean_content_over_limit() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                BoardCategory.FREE, "제목", "가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH + 1), null);

        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(boardService, never()).create(any());
    }

    @Test
    @DisplayName("제목 없는 등록 시 400")
    void create_fail_blank_title() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(BoardCategory.FREE, "", "본문", null);

        // when & then
        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(boardService, never()).create(any());
    }

    @Test
    @DisplayName("쓰기 권한 없는 공지 등록 시 403")
    void create_fail_notice_without_write_permission() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(BoardCategory.NOTICE, "공지", "본문", null);
        willThrow(new BusinessException(BoardErrorCode.NOTICE_REQUIRES_WRITE_PERMISSION))
                .given(boardService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void update_success() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest(BoardCategory.FREE, "수정 제목", "수정 본문", null);

        // when & then
        mockMvc.perform(put("/api/v1/boards/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(boardService).update(eq(7L), any());
    }

    @Test
    @DisplayName("한글 본문이 4,000자를 넘으면 수정 서비스 호출 전에 400")
    void update_rejects_korean_content_over_limit() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest(
                BoardCategory.FREE, "수정 제목", "가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH + 1), null);

        mockMvc.perform(put("/api/v1/boards/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(boardService, never()).update(any(), any());
    }

    @Test
    @DisplayName("작성자 아닌 수정 시 403")
    void update_fail_not_author() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest(BoardCategory.FREE, "수정 제목", "수정 본문", null);
        willThrow(new BusinessException(BoardErrorCode.NOT_AUTHOR))
                .given(boardService).update(eq(7L), any());

        // when & then
        mockMvc.perform(put("/api/v1/boards/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/boards/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(boardService).delete(7L);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void add_comment_success() throws Exception {
        // given
        given(boardService.addComment(eq(7L), any())).willReturn(10L);

        // when & then
        mockMvc.perform(post("/api/v1/boards/{id}/comments", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("댓글"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("빈 댓글 등록 시 400")
    void add_comment_fail_blank_content() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/boards/{id}/comments", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest(""))))
                .andExpect(status().isBadRequest());
        verify(boardService, never()).addComment(any(), any());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void delete_comment_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/boards/{postId}/comments/{commentId}", 7L, 10L))
                .andExpect(status().isNoContent());
        verify(boardService).deleteComment(7L, 10L);
    }

    @Test
    @DisplayName("작성자 아닌 댓글 삭제 시 403")
    void delete_comment_fail_not_author() throws Exception {
        // given
        willThrow(new BusinessException(BoardErrorCode.NOT_AUTHOR))
                .given(boardService).deleteComment(7L, 10L);

        // when & then
        mockMvc.perform(delete("/api/v1/boards/{postId}/comments/{commentId}", 7L, 10L))
                .andExpect(status().isForbidden());
    }
}
