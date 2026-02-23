package com.ohgiraffers.qa.e2etest.ui.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public final class DbCleaner {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/jpa_lecture";
    private static final String DB_USER = "iroha";
    private static final String DB_PW = "iroha";

    private DbCleaner() {}

    /** 테스트에서 만든 게시글만 삭제 */
    public static void deleteTestBoardsByPrefix() {
        exec("DELETE FROM board WHERE title LIKE 'post\\_%'");
    }


    private static void exec(String sql) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[DbCleaner] failed: " + e.getMessage());
        }
    }
}
