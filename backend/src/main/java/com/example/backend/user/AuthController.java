package com.example.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public record Credentials(String username, String password) {
    }

    public record UserResponse(Long id, String username) {
        static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getUsername());
        }
    }

    public record LoginResponse(String token, UserResponse user) {
        static LoginResponse from(AuthService.Session session) {
            return new LoginResponse(session.token(), UserResponse.from(session.user()));
        }
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse signup(@RequestBody Credentials body) {
        return LoginResponse.from(authService.signup(body.username(), body.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody Credentials body) {
        return LoginResponse.from(authService.login(body.username(), body.password()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(authorization);
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return UserResponse.from(authService.requireUser(authorization));
    }
}
