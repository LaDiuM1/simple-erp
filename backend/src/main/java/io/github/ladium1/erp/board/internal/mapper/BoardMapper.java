package io.github.ladium1.erp.board.internal.mapper;

import io.github.ladium1.erp.board.internal.dto.PostAttachmentResponse;
import io.github.ladium1.erp.board.internal.dto.PostCommentResponse;
import io.github.ladium1.erp.board.internal.dto.PostDetailResponse;
import io.github.ladium1.erp.board.internal.dto.PostSummaryResponse;
import io.github.ladium1.erp.board.internal.entity.Post;
import io.github.ladium1.erp.board.internal.entity.PostComment;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "category", source = "post.category")
    @Mapping(target = "createdAt", source = "post.createdAt")
    PostSummaryResponse toSummaryResponse(Post post, String authorName, long commentCount);

    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "category", source = "post.category")
    @Mapping(target = "authorId", source = "post.authorId")
    @Mapping(target = "createdAt", source = "post.createdAt")
    PostDetailResponse toDetailResponse(Post post,
                                        String authorName,
                                        List<PostAttachmentResponse> attachments,
                                        List<PostCommentResponse> comments);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "authorId", source = "comment.authorId")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    PostCommentResponse toCommentResponse(PostComment comment, String authorName);

    @Mapping(target = "fileId", source = "id")
    @Mapping(target = "name", source = "originalName")
    PostAttachmentResponse toAttachmentResponse(StoredFileInfo info);

    List<PostAttachmentResponse> toAttachmentResponses(List<StoredFileInfo> infos);
}
