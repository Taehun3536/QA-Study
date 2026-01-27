package com.ohgiraffers.qa.e2etest.api.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.qa.model.Board;
import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.repository.BoardRepository;
import com.ohgiraffers.qa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

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

    protected User createUser(String loginId, String password, String name) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setNickname(name);
        return userRepository.save(user);
    }

    protected MockHttpSession loginSession(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", loginId)
                        .param("password", password))
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
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
