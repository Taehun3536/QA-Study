package com.ohgiraffers.qa.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
