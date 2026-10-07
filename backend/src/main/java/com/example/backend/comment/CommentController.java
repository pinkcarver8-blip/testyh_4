package com.example.backend.comment;

import com.example.backend.user.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    public record CommentRequest(String content) {
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentService.CommentResponse> list(@PathVariable Long postId) {
        return commentService.list(postId);
    }

    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentService.CommentResponse create(@PathVariable Long postId, @RequestBody CommentRequest body,
                                                 @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commentService.create(postId, body.content(), authService.requireUser(authorization));
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        commentService.delete(id, authService.requireUser(authorization));
    }
}
