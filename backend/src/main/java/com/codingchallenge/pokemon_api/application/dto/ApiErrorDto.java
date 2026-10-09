package com.codingchallenge.pokemon_api.application.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorDto(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {}
