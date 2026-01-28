package com.ohgiraffers.qa.exception;

public class BoardIdNullException extends RuntimeException {
    public BoardIdNullException() {
        super("boardId는 필수입니다.");
    }
}
