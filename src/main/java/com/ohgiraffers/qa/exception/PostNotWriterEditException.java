package com.ohgiraffers.qa.exception;

public class PostNotWriterEditException extends RuntimeException {
    public PostNotWriterEditException() {
        super("작성자만 수정할 수 있습니다.");
    }
}
