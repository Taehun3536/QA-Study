package com.ohgiraffers.qa.api;

import com.ohgiraffers.qa.model.Board;
import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostApiController {

    private final BoardService boardService;

    public PostApiController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<Board>> list() {
        return ResponseEntity.ok(boardService.findAll());
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> get(@PathVariable Long id) {
        return boardService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<Board> create(@RequestBody Board board, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        Board saved = boardService.create(board, loginUser);
        return ResponseEntity.status(201).body(saved);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Board> update(@PathVariable Long id, @RequestBody Board board, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        Board updated = boardService.update(board, loginUser);
        return ResponseEntity.ok(updated);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        boardService.deleteById(id, loginUser);
        return ResponseEntity.noContent().build();
    }
}
