package io.github.ladium1.erp.board.internal.web;

import io.github.ladium1.erp.board.internal.dto.CommentCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostAttachmentDownload;
import io.github.ladium1.erp.board.internal.dto.PostCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostDetailResponse;
import io.github.ladium1.erp.board.internal.dto.PostSearchCondition;
import io.github.ladium1.erp.board.internal.dto.PostSummaryResponse;
import io.github.ladium1.erp.board.internal.dto.PostUpdateRequest;
import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import io.github.ladium1.erp.board.internal.service.BoardService;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시판 — 전 직원 개방 게시판이라 전 엔드포인트 CAN_READ.
 * 작성자 본인 검증 / NOTICE 카테고리의 write 권한 검사는 서비스가 담당.
 */
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private static final String MENU_CODE = "BOARDS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final BoardService boardService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<PostSummaryResponse> search(
            @RequestParam(required = false) BoardCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return boardService.search(new PostSearchCondition(category, keyword, authorId), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public PostDetailResponse getDetail(@PathVariable Long id) {
        return boardService.getDetail(id);
    }

    @GetMapping("/{id}/attachments/{fileId}")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadAttachment(@PathVariable Long id, @PathVariable Long fileId) {
        PostAttachmentDownload download = boardService.downloadAttachment(id, fileId);
        return DownloadResponse.attachment(download.content(), download.name(), download.contentType());
    }

    @PostMapping
    @PreAuthorize(CAN_READ)
    public Long create(@Valid @RequestBody PostCreateRequest request) {
        return boardService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public void update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        boardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public void delete(@PathVariable Long id) {
        boardService.delete(id);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize(CAN_READ)
    public Long addComment(@PathVariable Long id, @Valid @RequestBody CommentCreateRequest request) {
        return boardService.addComment(id, request);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize(CAN_READ)
    public void deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        boardService.deleteComment(postId, commentId);
    }
}
