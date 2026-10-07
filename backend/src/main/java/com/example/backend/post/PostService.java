package com.example.backend.post;

import com.example.backend.comment.CommentRepository;
import com.example.backend.common.Texts;
import com.example.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public record PostSummary(Long id, String title, String author, LocalDateTime createdAt, long commentCount) {
    }

    public record PostPage(List<PostSummary> posts, int page, int totalPages, long totalElements) {
    }

    public record PostDetail(Long id, String title, String content, Long authorId, String author,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        static PostDetail from(Post post) {
            return new PostDetail(post.getId(), post.getTitle(), post.getContent(),
                    post.getAuthor().getId(), post.getAuthor().getUsername(),
                    post.getCreatedAt(), post.getUpdatedAt());
        }
    }

    @Transactional(readOnly = true)
    public PostPage list(int page) {
        Page<Post> result = postRepository.findAllByOrderByIdDesc(PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        List<Long> ids = result.getContent().stream().map(Post::getId).toList();

        Map<Long, Long> commentCounts = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : commentRepository.countByPostIds(ids)) {
                commentCounts.put((Long) row[0], (Long) row[1]);
            }
        }

        List<PostSummary> posts = result.getContent().stream()
                .map(p -> new PostSummary(p.getId(), p.getTitle(), p.getAuthor().getUsername(),
                        p.getCreatedAt(), commentCounts.getOrDefault(p.getId(), 0L)))
                .toList();
        return new PostPage(posts, result.getNumber(), result.getTotalPages(), result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostDetail get(Long id) {
        return PostDetail.from(find(id));
    }

    @Transactional
    public PostDetail create(String title, String content, User author) {
        Post post = new Post(Texts.require(title, "제목", 200), Texts.require(content, "내용", 20000), author);
        return PostDetail.from(postRepository.save(post));
    }

    @Transactional
    public PostDetail update(Long id, String title, String content, User user) {
        Post post = findOwned(id, user);
        post.update(Texts.require(title, "제목", 200), Texts.require(content, "내용", 20000));
        return PostDetail.from(post);
    }

    @Transactional
    public void delete(Long id, User user) {
        Post post = findOwned(id, user);
        commentRepository.deleteAllByPostId(post.getId());
        postRepository.delete(post);
    }

    public Post find(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private Post findOwned(Long id, User user) {
        Post post = find(id);
        if (!post.isWrittenBy(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자 본인만 수정하거나 삭제할 수 있습니다.");
        }
        return post;
    }
}
