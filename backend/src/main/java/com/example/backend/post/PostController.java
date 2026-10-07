package com.example.backend.post;

import com.example.backend.user.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public record PostRequest(String title, String content) {
    }

    @GetMapping
    public PostService.PostPage list(@RequestParam(defaultValue = "0") int page) {
        return postService.list(page);
    }

    @GetMapping("/{id}")
    public PostService.PostDetail get(@PathVariable Long id) {
        return postService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostService.PostDetail create(@RequestBody PostRequest body,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return postService.create(body.title(), body.content(), authService.requireUser(authorization));
    }

    @PutMapping("/{id}")
    public PostService.PostDetail update(@PathVariable Long id, @RequestBody PostRequest body,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return postService.update(id, body.title(), body.content(), authService.requireUser(authorization));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        postService.delete(id, authService.requireUser(authorization));
    }
}
