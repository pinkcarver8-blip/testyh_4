package com.example.backend;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(
        origins = "http://localhost:5173",
        originPatterns = "https://*.vercel.app"
)
public class HelloController {

    @GetMapping(value = "/", produces = "text/plain;charset=UTF-8")
    public String home() {
        return "백엔드 서버가 정상 작동 중입니다.";
    }

    @GetMapping(value = "/api/hello", produces = "text/plain;charset=UTF-8")
    public String hello() {
        return "안녕하세요! 백엔드에서 보낸 메시지입니다.";
    }
}