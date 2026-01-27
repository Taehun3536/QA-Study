package com.ohgiraffers.qa.repository;

import com.ohgiraffers.qa.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long> {
}
