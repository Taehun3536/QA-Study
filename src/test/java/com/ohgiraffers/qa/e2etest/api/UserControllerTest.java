package com.ohgiraffers.qa.e2etest.api;

import com.ohgiraffers.qa.e2etest.api.base.ApiTestBase;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends ApiTestBase {

    @Test
    void tc1_join_success() throws Exception {
        // Given
        userRepository.deleteAll();

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", "test1")
                        .param("password", "1234")
                        .param("nickname", "test1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/login"));

        // Then: 실제 저장 확인(추천)
        assertThat(userRepository.findByLoginId("test1")).isPresent();
    }


    @Test
    void tc2_join_fail_duplicate_id() throws Exception {
        // Given
        userRepository.deleteAll();
        createUser("test_dup", "1234", "DUP");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", "test_dup")
                        .param("password", "1234")
                        .param("nickname", "DUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이미 존재하는 아이디입니다.")));
    }

    @Test
    void tc3_login_success() throws Exception {
        // Given
        userRepository.deleteAll();
        createUser("test_login", "1234", "test_login");

        // When
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", "test_login")
                        .param("password", "1234"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/board/list"))
                .andReturn();

        // Then: 세션 생성 + user 저장 확인
        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("user")).isNotNull();
    }

    @Test
    void tc4_login_fail() throws Exception {
        // Given
        userRepository.deleteAll();
        createUser("test_login2", "1234", "test_login2");

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", "test_login2")
                        .param("password", "wrong_pw"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/login-fail"));
    }
}

