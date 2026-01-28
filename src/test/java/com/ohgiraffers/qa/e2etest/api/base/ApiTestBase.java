package com.ohgiraffers.qa.e2etest.api.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.repository.BoardRepository;
import com.ohgiraffers.qa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected BoardRepository boardRepository;
    @Autowired protected UserRepository userRepository;

    /**
     * 테스트 데이터 누적 방지
     * FK가 걸려있을 수 있으니 Board -> User 순서로 삭제
     */
    @BeforeEach
    void cleanDatabase() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String loginId, String password, String nickname) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    protected MockHttpSession loginSession(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", loginId)
                        .param("password", password))
                // 로그인은 성공해야 세션이 생김 (실패하면 아래에서 null 됨)
                .andExpect(status().isFound())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).as("로그인 성공 시 세션이 생성되어야 합니다.").isNotNull();
        assertThat(session.getAttribute("user")).as("세션에 user가 있어야 합니다.").isNotNull();
        return session;
    }

    protected Long createPostAndGetId(MockHttpSession session, String title, String content) throws Exception {
        String body = """
        {"title":"%s","content":"%s"}
        """.formatted(title, content);

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("boardId").asLong();
    }
}
