package com.ohgiraffers.qa.controller;

import com.ohgiraffers.qa.model.Board;
import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<Board> boards = boardService.findAll();
        model.addAttribute("boards", boards);
        return "board";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "board_write";
    }

    @PostMapping("/save")
    public String create(@ModelAttribute Board board, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");

        if (loginUser == null) return "redirect:/user/login";

        board.setWriterId(loginUser.getId());
        board.setWriterName(loginUser.getNickname());

        boardService.create(board, loginUser);
        return "redirect:/board/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) return "redirect:/user/login";

        Board board = boardService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + id));

        model.addAttribute("board", board);
        return "board_edit";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        boardService.findById(id).ifPresent(b -> model.addAttribute("board", b));
        return "board_detail";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Board board, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");

        boardService.update(board, loginUser);
        return "redirect:/board/list";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) return "redirect:/user/login";

        try {
            boardService.deleteById(id, loginUser);
            return "redirect:/board/list";
        } catch (RuntimeException e) {
            // 권한 없을 때 화면 터뜨리지 말고 목록으로 보내기
            return "redirect:/board/list";
        }
    }
}
