package com.ohgiraffers.qa.api;

import com.ohgiraffers.qa.exception.BoardIdNullException;
import com.ohgiraffers.qa.exception.PostNotFoundException;
import com.ohgiraffers.qa.exception.PostNotWriterDeleteException;
import com.ohgiraffers.qa.exception.PostNotWriterEditException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.ohgiraffers.qa.api")
public class ApiExceptionHandler {

    // @Valid 검증 실패 시 발생하는 예외 처리 추가
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

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