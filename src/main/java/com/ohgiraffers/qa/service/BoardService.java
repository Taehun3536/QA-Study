package com.ohgiraffers.qa.service;

import com.ohgiraffers.qa.model.Board;
import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    /**
     * 게시글 생성
     * */
    @Transactional
    public Board create(Board board, User loginUser) {
        board.setWriterId(loginUser.getId());
        board.setWriterName(loginUser.getNickname());
        return boardRepository.save(board);
    }

    /**
     * 게시글 수정
     * */
    @Transactional
    public Board update(Board board, User loginUser) {

        if (board.getBoardId() == null) {
            throw new IllegalArgumentException("boardId는 필수입니다.");
        }

        Board existing = boardRepository.findById(board.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!existing.getWriterId().equals(loginUser.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        board.setWriterId(existing.getWriterId());
        board.setWriterName(existing.getWriterName());

        return boardRepository.save(board);
    }


    /**
     * 게시글 삭제
     * */
    public void deleteById(Long id, User loginUser) {
        Board existing = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + id));

        if (existing.getWriterId() != null && !existing.getWriterId().equals(loginUser.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        boardRepository.deleteById(id);
    }
}
