package com.ohgiraffers.qa.api;

import com.ohgiraffers.qa.exception.BoardIdNullException;
import com.ohgiraffers.qa.exception.PostNotFoundException;
import com.ohgiraffers.qa.exception.PostNotWriterDeleteException;
import com.ohgiraffers.qa.exception.PostNotWriterEditException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ohgiraffers.qa.api")
public class ApiExceptionHandler {
    @ExceptionHandler(BoardIdNullException.class)
    public ResponseEntity<String> handleBoardIdNull(BoardIdNullException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<String> handleNotFound(PostNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler({
            PostNotWriterEditException.class,
            PostNotWriterDeleteException.class
    })
    public ResponseEntity<String> handleForbidden(RuntimeException e) {
        return ResponseEntity.status(403).body(e.getMessage());
    }
}
