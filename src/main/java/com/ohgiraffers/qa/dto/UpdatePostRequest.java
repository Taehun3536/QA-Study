package com.ohgiraffers.qa.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
