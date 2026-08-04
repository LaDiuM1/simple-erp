package io.github.ladium1.erp.board.internal.service;

import io.github.ladium1.erp.board.internal.dto.CommentCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostAttachmentDownload;
import io.github.ladium1.erp.board.internal.dto.PostCreateRequest;
import io.github.ladium1.erp.board.internal.dto.PostUpdateRequest;
import io.github.ladium1.erp.board.internal.entity.BoardCategory;
import io.github.ladium1.erp.board.internal.entity.Post;
import io.github.ladium1.erp.board.internal.entity.PostComment;
import io.github.ladium1.erp.board.internal.exception.BoardErrorCode;
import io.github.ladium1.erp.board.internal.mapper.BoardMapper;
import io.github.ladium1.erp.board.internal.repository.PostCommentRepository;
import io.github.ladium1.erp.board.internal.repository.PostRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.storage.internal.exception.StorageErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock private PostRepository postRepository;
    @Mock private PostCommentRepository postCommentRepository;
    @Mock private BoardMapper boardMapper;
    @Mock private EmployeeApi employeeApi;
    @Mock private FileStorageApi fileStorageApi;
    @Mock private MenuPermissionEvaluator menuPermissionEvaluator;

    private static final String TEST_LOGIN_ID = "testUser";
    private static final Long MY_EMPLOYEE_ID = 1L;
    private static final Long OTHER_EMPLOYEE_ID = 2L;

    @BeforeEach
    void set_authentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_LOGIN_ID, null, List.of()));
    }

    @AfterEach
    void clear_authentication() {
        SecurityContextHolder.clearContext();
    }

    private void stubCurrentEmployee() {
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(
                EmployeeInfo.builder().id(MY_EMPLOYEE_ID).name("테스트직원").build()
        ));
    }

    private Post post(BoardCategory category, Long authorId) {
        return post(category, authorId, List.of());
    }

    private Post post(BoardCategory category, Long authorId, List<Long> attachmentFileIds) {
        return Post.builder()
                .category(category)
                .title("제목")
                .content("본문")
                .authorId(authorId)
                .attachmentFileIds(attachmentFileIds)
                .build();
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void create_success() {
        // given
        stubCurrentEmployee();
        PostCreateRequest request = new PostCreateRequest(BoardCategory.FREE, "제목", "본문", List.of(10L));

        Post saved = post(BoardCategory.FREE, MY_EMPLOYEE_ID);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(postRepository.save(any(Post.class))).willReturn(saved);

        // when
        Long id = boardService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        var order = inOrder(postRepository, fileStorageApi);
        order.verify(postRepository).save(captor.capture());
        order.verify(fileStorageApi).claim(
                List.of(10L), FileOwner.boardPost(100L), MY_EMPLOYEE_ID);
        assertThat(captor.getValue().getAuthorId()).isEqualTo(MY_EMPLOYEE_ID);
        assertThat(captor.getValue().getAttachmentFileIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("쓰기 권한 없는 공지 작성 거부")
    void create_fail_notice_without_write_permission() {
        // given
        PostCreateRequest request = new PostCreateRequest(BoardCategory.NOTICE, "공지", "본문", null);
        given(menuPermissionEvaluator.canWrite(any(), eq("BOARDS"))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> boardService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.NOTICE_REQUIRES_WRITE_PERMISSION);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("작성자 아닌 수정 — NOT_AUTHOR")
    void update_fail_not_author() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, OTHER_EMPLOYEE_ID);
        given(postRepository.findById(5L)).willReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(BoardCategory.FREE, "수정 제목", "수정 본문", null);

        // when & then
        assertThatThrownBy(() -> boardService.update(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.NOT_AUTHOR);
    }

    @Test
    @DisplayName("게시글 삭제 성공 — 댓글 + 첨부 파일도 함께 제거")
    void delete_success() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID, List.of(10L, 11L));
        ReflectionTestUtils.setField(post, "id", 5L);
        given(postRepository.findById(5L)).willReturn(Optional.of(post));

        // when
        boardService.delete(5L);

        // then
        var order = inOrder(fileStorageApi, postCommentRepository, postRepository);
        order.verify(fileStorageApi).requestDeletion(List.of(10L, 11L), FileOwner.boardPost(5L));
        order.verify(postCommentRepository).deleteByPostId(5L);
        order.verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 수정 — 제외된 첨부만 storage 에서 제거")
    void update_success_deletes_removed_attachments() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID, List.of(10L, 11L));
        given(postRepository.findById(5L)).willReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(BoardCategory.FREE, "수정 제목", "수정 본문", List.of(11L, 12L));

        // when
        boardService.update(5L, request);

        // then
        assertThat(post.getAttachmentFileIds()).containsExactly(11L, 12L);
        var order = inOrder(fileStorageApi);
        order.verify(fileStorageApi).claim(
                List.of(11L, 12L), FileOwner.boardPost(5L), MY_EMPLOYEE_ID);
        order.verify(fileStorageApi).requestDeletion(List.of(10L), FileOwner.boardPost(5L));
    }

    @Test
    @DisplayName("첨부 소유권 검증 실패 시 게시글과 기존 첨부를 변경하지 않음")
    void update_stops_before_domain_change_when_claim_fails() {
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID, List.of(10L));
        given(postRepository.findById(5L)).willReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(
                BoardCategory.FREE, "수정 제목", "수정 본문", List.of(11L));
        willThrow(new BusinessException(StorageErrorCode.FILE_CLAIM_NOT_ALLOWED))
                .given(fileStorageApi)
                .claim(List.of(11L), FileOwner.boardPost(5L), MY_EMPLOYEE_ID);

        assertThatThrownBy(() -> boardService.update(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StorageErrorCode.FILE_CLAIM_NOT_ALLOWED);

        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getAttachmentFileIds()).containsExactly(10L);
        verify(fileStorageApi, never()).requestDeletion(any(), any());
    }

    @Test
    @DisplayName("작성자 아닌 삭제 — NOT_AUTHOR")
    void delete_fail_not_author() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, OTHER_EMPLOYEE_ID);
        given(postRepository.findById(5L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> boardService.delete(5L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.NOT_AUTHOR);
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void add_comment_success() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, OTHER_EMPLOYEE_ID);
        ReflectionTestUtils.setField(post, "id", 5L);
        given(postRepository.findById(5L)).willReturn(Optional.of(post));

        PostComment saved = PostComment.builder().post(post).authorId(MY_EMPLOYEE_ID).content("댓글").build();
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(postCommentRepository.save(any(PostComment.class))).willReturn(saved);

        // when
        Long id = boardService.addComment(5L, new CommentCreateRequest("댓글"));

        // then
        assertThat(id).isEqualTo(10L);
        ArgumentCaptor<PostComment> captor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthorId()).isEqualTo(MY_EMPLOYEE_ID);
    }

    @Test
    @DisplayName("작성자 아닌 댓글 삭제 거부")
    void delete_comment_fail_not_author() {
        // given
        stubCurrentEmployee();
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID);
        ReflectionTestUtils.setField(post, "id", 5L);
        PostComment comment = PostComment.builder().post(post).authorId(OTHER_EMPLOYEE_ID).content("댓글").build();
        given(postCommentRepository.findById(10L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> boardService.deleteComment(5L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.NOT_AUTHOR);
        verify(postCommentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("첨부 다운로드 성공")
    void download_attachment_success() {
        // given
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID, List.of(10L));
        given(postRepository.findById(5L)).willReturn(Optional.of(post));
        FileOwner owner = FileOwner.boardPost(5L);
        given(fileStorageApi.getInfo(10L, owner)).willReturn(
                StoredFileInfo.builder().id(10L).originalName("자료.pdf").contentType("application/pdf").build());
        given(fileStorageApi.loadContent(10L, owner)).willReturn("pdf-bytes".getBytes());

        // when
        PostAttachmentDownload download = boardService.downloadAttachment(5L, 10L);

        // then
        assertThat(download.name()).isEqualTo("자료.pdf");
        assertThat(download.contentType()).isEqualTo("application/pdf");
        assertThat(download.content()).isEqualTo("pdf-bytes".getBytes());
    }

    @Test
    @DisplayName("게시글에 연결되지 않은 첨부 다운로드 시 404 은닉")
    void download_attachment_fail_not_attached() {
        // given
        Post post = post(BoardCategory.FREE, MY_EMPLOYEE_ID, List.of(10L));
        given(postRepository.findById(5L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> boardService.downloadAttachment(5L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.POST_NOT_FOUND);
        verify(fileStorageApi, never()).loadContent(any(), any());
    }
}
