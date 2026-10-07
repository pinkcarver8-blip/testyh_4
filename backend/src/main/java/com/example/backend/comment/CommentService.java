package com.example.backend.comment;

import com.example.backend.common.Texts;
import com.example.backend.post.PostService;
import com.example.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    public record CommentResponse(Long id, String content, Long authorId, String author, LocalDateTime createdAt) {
        static CommentResponse from(Comment comment) {
            return new CommentResponse(comment.getId(), comment.getContent(),
                    comment.getAuthor().getId(), comment.getAuthor().getUsername(), comment.getCreatedAt());
        }
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        postService.find(postId);
        return commentRepository.findAllByPostIdOrderByIdAsc(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long postId, String content, User author) {
        Comment comment = new Comment(Texts.require(content, "댓글", 1000), postService.find(postId), author);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!comment.isWrittenBy(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자 본인만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }
}
