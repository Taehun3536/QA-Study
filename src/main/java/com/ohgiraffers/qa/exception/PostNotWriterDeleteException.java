package com.ohgiraffers.qa.exception;

public class PostNotWriterDeleteException extends RuntimeException {
    public PostNotWriterDeleteException() {
        super("작성자만 삭제할 수 있습니다.");
    }
}
