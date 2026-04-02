package com.ohgiraffers.qa.e2etest.api;

import com.ohgiraffers.qa.e2etest.api.base.ApiTestBase;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("사용자 계정 및 인증 API 테스트")
class UserControllerTest extends ApiTestBase {

    @Nested
    @DisplayName("회원가입 기능 테스트")
    class JoinTest {

        @Test
        @DisplayName("유효한 정보를 입력하면 회원가입에 성공하고 로그인 페이지로 리다이렉트된다.")
        void tc1_join_success() throws Exception {
            mockMvc.perform(post("/user/join")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("loginId", "newPlayer")
                            .param("password", "pass123")
                            .param("nickname", "루미아생존자"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/login"));

            assertThat(userRepository.findByLoginId("newPlayer")).isPresent();
        }

        @Test
        @DisplayName("이미 존재하는 아이디로 가입 시도 시 에러 메시지를 반환한다.")
        void tc2_join_fail_duplicate_id() throws Exception {
            createUser("dup_user", "1234", "기존닉네임");

            mockMvc.perform(post("/user/join")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("loginId", "dup_user")
                            .param("password", "1234")
                            .param("nickname", "새닉네임"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("이미 존재하는 아이디입니다.")));
        }
    }

    @Nested
    @DisplayName("로그인 기능 테스트")
    class LoginTest {

        @Test
        @DisplayName("등록된 계정으로 로그인하면 세션이 생성되고 메인 페이지로 이동한다.")
        void tc3_login_success() throws Exception {
            createUser("login_user", "pw123", "테스터");

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("loginId", "login_user")
                            .param("password", "pw123"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/board/list"))
                    .andReturn();

            HttpSession session = result.getRequest().getSession(false);
            assertThat(session).isNotNull();
            assertThat(session.getAttribute("user")).isNotNull();
        }

        @ParameterizedTest
        @CsvSource({
                "login_user, wrong_pw",
                "non_exist, pw123"
        })
        @DisplayName("잘못된 비밀번호나 존재하지 않는 아이디로 로그인 시 실패 페이지로 이동한다.")
        void tc4_login_fail(String id, String pw) throws Exception {
            createUser("login_user", "pw123", "테스터");

            mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("loginId", id)
                            .param("password", pw))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/login-fail"));
        }
    }
}