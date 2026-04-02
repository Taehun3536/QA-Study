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
import java.util.Map;
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

    @BeforeEach
    void cleanDatabase() {
        // FK 제약 조건에 따른 순차 삭제 유지
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
                .andExpect(status().isFound())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).as("로그인 성공 시 세션이 생성되어야 합니다.").isNotNull();
        assertThat(session.getAttribute("user")).as("세션에 user 데이터가 포함되어야 합니다.").isNotNull();
        return session;
    }

    /**
     * 수동 문자열 결합 대신 Map과 ObjectMapper를 사용하여 JSON 생성의 안전성 확보
     */
    protected Long createPostAndGetId(MockHttpSession session, String title, String content) throws Exception {
        Map<String, String> postData = Map.of("title", title, "content", content);

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postData)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("boardId").asLong();
    }
}