package com.ohgiraffers.qa.e2etest.api;

import com.ohgiraffers.qa.e2etest.api.base.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostApiTest extends ApiTestBase {

    @Test
    void tc1_create_post_when_login() throws Exception {
        // Given
        createUser("test1", "1234", "test1");
        MockHttpSession session = loginSession("test1", "1234");

        String body = """
        {
          "title": "제목",
          "content": "내용"
        }
        """;

        // When & Then
        mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").exists())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("내용"))
                .andExpect(jsonPath("$.writerId").exists())
                .andExpect(jsonPath("$.writerName").value("test1"));
    }

    @Test
    void tc2_create_post_when_not_login() throws Exception {
        String body = """
        {
          "title": "제목",
          "content": "내용"
        }
        """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tc3_board_list_not_post() throws Exception {
        // Given
        boardRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void tc4_board_list_post() throws Exception {
        // Given
        boardRepository.deleteAll();
        createUser("test1", "1234", "test1");
        MockHttpSession session = loginSession("test1", "1234");

        Long id = createPostAndGetId(session, "제목", "내용"); // 생성 자체는 헬퍼로

        // When & Then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // 최소 1개는 존재해야 함
                .andExpect(jsonPath("$[0].boardId").exists())
                // 방금 만든 글이 포함되어 있는지(느슨한 검증)
                .andExpect(content().string(containsString("\"boardId\":" + id)))
                .andExpect(content().string(containsString("\"title\":\"제목\"")))
                .andExpect(content().string(containsString("\"content\":\"내용\"")))
                .andExpect(content().string(containsString("\"writerName\":\"test1\"")));
    }

    @Test
    void tc5_post_update_when_writer() throws Exception {
        // Given
        createUser("test1", "1234", "test1");
        MockHttpSession session = loginSession("test1", "1234");

        Long id = createPostAndGetId(session, "제목", "내용");

        String updateBody = """
        {
          "boardId": %d,
          "title": "수정",
          "content": "수정"
        }
        """.formatted(id);

        // When
        mockMvc.perform(put("/api/posts/{id}", id)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        // Then
        mockMvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(id))
                .andExpect(jsonPath("$.title").value("수정"))
                .andExpect(jsonPath("$.content").value("수정"));
    }

    @Test
    void tc6_post_update_when_not_writer() throws Exception {
        // Given
        boardRepository.deleteAll();

        createUser("test1", "1234", "test1"); // A
        createUser("test2", "1234", "test2"); // B

        MockHttpSession sessionA = loginSession("test1", "1234");
        MockHttpSession sessionB = loginSession("test2", "1234");

        Long id = createPostAndGetId(sessionA, "제목", "내용");

        String updateBody = """
        {
          "boardId": %d,
          "title": "수정",
          "content": "수정"
        }
        """.formatted(id);

        // When: B가 수정 시도
        mockMvc.perform(put("/api/posts/{id}", id)
                        .session(sessionB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());

        // Then: 변경되지 않음
        mockMvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(id))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("내용"));
    }

    @Test
    void tc7_post_delete_when_writer() throws Exception {
        // Given
        createUser("test1", "1234", "test1");
        MockHttpSession session = loginSession("test1", "1234");

        Long id = createPostAndGetId(session, "제목", "내용");

        // When & Then: 삭제 성공
        mockMvc.perform(delete("/api/posts/{id}", id)
                        .session(session))
                .andExpect(status().isNoContent());

        // Then: 재조회 시 404
        mockMvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void tc8_post_delete_when_not_writer() throws Exception {
        // Given
        boardRepository.deleteAll();

        createUser("test1", "1234", "test1"); // A
        createUser("test2", "1234", "test2"); // B

        MockHttpSession sessionA = loginSession("test1", "1234");
        MockHttpSession sessionB = loginSession("test2", "1234");

        Long id = createPostAndGetId(sessionA, "제목", "내용");

        // When: B가 삭제 시도
        mockMvc.perform(delete("/api/posts/{id}", id)
                        .session(sessionB))
                .andExpect(status().isForbidden());

        // Then: 삭제되지 않음
        mockMvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(id))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("내용"));
    }

    @Test
    void tc9_create_post_when_title_empty_returns_400() throws Exception {
        createUser("test1", "1234", "test1");
        MockHttpSession session = loginSession("test1", "1234");

        String body = """
		    { "title": "", "content": "내용" }
		    """;

        mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

