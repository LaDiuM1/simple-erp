package io.github.ladium1.erp.board.internal.service;

import io.github.ladium1.erp.board.internal.dto.CommentCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostAttachmentDownload;
import io.github.ladium1.erp.board.internal.dto.PostAttachmentResponse;
import io.github.ladium1.erp.board.internal.dto.PostCommentResponse;
import io.github.ladium1.erp.board.internal.dto.PostCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostDetailResponse;
import io.github.ladium1.erp.board.internal.dto.PostSearchCondition;
import io.github.ladium1.erp.board.internal.dto.PostSummaryResponse;
import io.github.ladium1.erp.board.internal.dto.PostUpdateRequest;
import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import io.github.ladium1.erp.board.internal.entity.Post;
import io.github.ladium1.erp.board.internal.entity.PostComment;
import io.github.ladium1.erp.board.internal.exception.BoardErrorCode;
import io.github.ladium1.erp.board.internal.mapper.BoardMapper;
import io.github.ladium1.erp.board.internal.repository.PostCommentCount;
import io.github.ladium1.erp.board.internal.repository.PostCommentRepository;
import io.github.ladium1.erp.board.internal.repository.PostRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * 게시판 — 전 직원 개방 (컨트롤러는 CAN_READ), 본인 작성물 검증과 NOTICE 쓰기 권한 검사는 서비스가 담당.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final BoardMapper boardMapper;
    private final EmployeeApi employeeApi;
    private final FileStorageApi fileStorageApi;
    private final MenuPermissionEvaluator menuPermissionEvaluator;

    public PageResponse<PostSummaryResponse> search(PostSearchCondition condition, Pageable pageable) {
        Page<Post> page = postRepository.search(condition, pageable);

        Map<Long, String> authorNames = authorNamesOf(
                page.getContent().stream().map(Post::getAuthorId).toList()
        );
        Map<Long, Long> commentCounts = commentCountsOf(page.getContent());

        return PageResponse.of(page.map(post -> boardMapper.toSummaryResponse(
                post,
                authorNames.get(post.getAuthorId()),
                commentCounts.getOrDefault(post.getId(), 0L)
        )));
    }

    public PostDetailResponse getDetail(Long id) {
        Post post = findPost(id);
        List<PostComment> comments = postCommentRepository.findByPostIdOrderByIdAsc(id);

        Map<Long, String> authorNames = authorNamesOf(
                Stream.concat(
                        Stream.of(post.getAuthorId()),
                        comments.stream().map(PostComment::getAuthorId)
                ).toList()
        );

        List<PostAttachmentResponse> attachments =
                boardMapper.toAttachmentResponses(fileStorageApi.getInfos(post.getAttachmentFileIds()));
        List<PostCommentResponse> commentResponses = comments.stream()
                .map(comment -> boardMapper.toCommentResponse(comment, authorNames.get(comment.getAuthorId())))
                .toList();

        return boardMapper.toDetailResponse(post, authorNames.get(post.getAuthorId()), attachments, commentResponses);
    }

    /**
     * 첨부 다운로드 — 해당 게시글에 연결된 파일인지 확인 (아니면 POST_NOT_FOUND 로 존재 은닉).
     */
    public PostAttachmentDownload downloadAttachment(Long postId, Long fileId) {
        Post post = findPost(postId);
        if (!post.getAttachmentFileIds().contains(fileId)) {
            throw new BusinessException(BoardErrorCode.POST_NOT_FOUND);
        }
        StoredFileInfo info = fileStorageApi.getInfo(fileId);
        return new PostAttachmentDownload(info.originalName(), info.contentType(), fileStorageApi.loadContent(fileId));
    }

    @Auditable(menu = Menu.BOARDS, action = AuditAction.CREATE, targetType = "Post", targetIdFromReturn = true)
    @Transactional
    public Long create(PostCreateRequest request) {
        requireWritePermissionForNotice(request.category());

        Post post = Post.builder()
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .authorId(currentEmployeeId())
                .attachmentFileIds(request.attachmentFileIds())
                .build();
        return postRepository.save(post).getId();
    }

    @Auditable(menu = Menu.BOARDS, action = AuditAction.UPDATE, targetType = "Post", targetIdParam = "id")
    @Transactional
    public void update(Long id, PostUpdateRequest request) {
        Post post = findPost(id);
        requireAuthor(post.isAuthor(currentEmployeeId()));
        requireWritePermissionForNotice(request.category());

        // 수정으로 제외된 첨부는 storage 에서도 제거 — 고아 파일 잔존 차단 (drive 의 파일 삭제 관습과 대칭)
        List<Long> requestedFileIds = request.attachmentFileIds() == null ? List.of() : request.attachmentFileIds();
        List<Long> removedFileIds = post.getAttachmentFileIds().stream()
                .filter(fileId -> !requestedFileIds.contains(fileId))
                .toList();

        post.update(request.category(), request.title(), request.content(), request.attachmentFileIds());
        removedFileIds.forEach(fileStorageApi::delete);
    }

    @Auditable(menu = Menu.BOARDS, action = AuditAction.DELETE, targetType = "Post", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        Post post = findPost(id);
        requireAuthor(post.isAuthor(currentEmployeeId()));

        List<Long> attachmentFileIds = List.copyOf(post.getAttachmentFileIds());
        postCommentRepository.deleteByPostId(id);
        postRepository.delete(post);
        attachmentFileIds.forEach(fileStorageApi::delete);
    }

    @Auditable(menu = Menu.BOARDS, action = AuditAction.CREATE, targetType = "PostComment", targetIdFromReturn = true)
    @Transactional
    public Long addComment(Long postId, CommentCreateRequest request) {
        Post post = findPost(postId);

        PostComment comment = PostComment.builder()
                .post(post)
                .authorId(currentEmployeeId())
                .content(request.content())
                .build();
        return postCommentRepository.save(comment).getId();
    }

    @Auditable(menu = Menu.BOARDS, action = AuditAction.DELETE, targetType = "PostComment", targetIdParam = "commentId")
    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(BoardErrorCode.COMMENT_NOT_FOUND);
        }
        requireAuthor(comment.isAuthor(currentEmployeeId()));

        postCommentRepository.delete(comment);
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.POST_NOT_FOUND));
    }

    private void requireAuthor(boolean isAuthor) {
        if (!isAuthor) {
            throw new BusinessException(BoardErrorCode.NOT_AUTHOR);
        }
    }

    /**
     * NOTICE 카테고리 작성/수정은 BOARDS 메뉴 write 권한 필요 — 컨트롤러는 CAN_READ 로 열려 있어 서비스에서 검사.
     */
    private void requireWritePermissionForNotice(BoardCategory category) {
        if (category != BoardCategory.NOTICE) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!menuPermissionEvaluator.canWrite(authentication, Menu.BOARDS.name())) {
            throw new BusinessException(BoardErrorCode.NOTICE_REQUIRES_WRITE_PERMISSION);
        }
    }

    private Long currentEmployeeId() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeApi.findByLoginId(loginId)
                .orElseThrow(() -> new AccessDeniedException("인증된 직원 정보를 찾을 수 없습니다."))
                .id();
    }

    private Map<Long, String> authorNamesOf(List<Long> authorIds) {
        return employeeApi.findByIds(authorIds.stream().distinct().toList()).stream()
                .collect(toMap(EmployeeInfo::id, EmployeeInfo::name));
    }

    private Map<Long, Long> commentCountsOf(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postCommentRepository.countByPostIds(postIds).stream()
                .collect(toMap(PostCommentCount::postId, PostCommentCount::count));
    }
}
