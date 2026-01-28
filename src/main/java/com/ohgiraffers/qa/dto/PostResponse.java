package com.ohgiraffers.qa.dto;

public record PostResponse(
        Long boardId,
        String title,
        String content,
        Long writerId,
        String writerName
) {
}
