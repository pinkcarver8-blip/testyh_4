package com.example.backend.user;

import com.example.backend.common.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BEARER = "Bearer ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public record Session(String token, User user) {
    }

    @Transactional
    public Session signup(String username, String password) {
        String name = Texts.require(username, "아이디", 30);
        if (password == null || password.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 4자 이상이어야 합니다.");
        }
        if (userRepository.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        User user = userRepository.save(new User(name, passwordEncoder.encode(password)));
        return new Session(issueToken(user), user);
    }

    @Transactional
    public Session login(String username, String password) {
        User user = userRepository.findByUsername(username == null ? "" : username.trim())
                .filter(u -> password != null && passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));
        return new Session(issueToken(user), user);
    }

    @Transactional
    public void logout(String authorization) {
        extractToken(authorization).ifPresent(authTokenRepository::deleteById);
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(String authorization) {
        return extractToken(authorization)
                .flatMap(authTokenRepository::findWithUserByToken)
                .map(AuthToken::getUser);
    }

    @Transactional(readOnly = true)
    public User requireUser(String authorization) {
        return findUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private String issueToken(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        authTokenRepository.save(new AuthToken(token, user));
        return token;
    }

    private Optional<String> extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER)) {
            return Optional.empty();
        }
        String token = authorization.substring(BEARER.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
