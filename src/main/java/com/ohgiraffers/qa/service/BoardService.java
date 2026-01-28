package com.ohgiraffers.qa.service;

import com.ohgiraffers.qa.exception.BoardIdNullException;
import com.ohgiraffers.qa.exception.PostNotFoundException;
import com.ohgiraffers.qa.exception.PostNotWriterDeleteException;
import com.ohgiraffers.qa.exception.PostNotWriterEditException;
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
            throw new BoardIdNullException();
        }

        Board existing = boardRepository.findById(board.getBoardId())
                .orElseThrow(PostNotFoundException::new);

        if (!existing.getWriterId().equals(loginUser.getId())) {
            throw new PostNotWriterEditException();
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
                .orElseThrow(() -> new PostNotFoundException(id));

        if (existing.getWriterId() != null && !existing.getWriterId().equals(loginUser.getId())) {
            throw new PostNotWriterDeleteException();
        }

        boardRepository.deleteById(id);
    }
}
