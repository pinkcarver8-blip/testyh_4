package com.example.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Texts {

    private Texts() {
    }

    /** 앞뒤 공백을 제거하고, 비어 있거나 너무 길면 400 에러를 던진다. */
    public static String require(String value, String field, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + "을(를) 입력해주세요.");
        }
        if (trimmed.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + "은(는) " + maxLength + "자 이하로 입력해주세요.");
        }
        return trimmed;
    }
}
