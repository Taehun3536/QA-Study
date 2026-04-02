package com.ohgiraffers.qa.e2etest.api;

import com.ohgiraffers.qa.e2etest.api.base.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@DisplayName("게시글 관련 API 시스템 테스트")
class PostApiTest extends ApiTestBase {

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePost {

        @Test
        @DisplayName("로그인한 사용자가 유효한 데이터를 입력하면 게시글이 정상적으로 생성된다.")
        void createPostSuccess() throws Exception {
            // Given: 전용 DTO 대신 Map을 사용하여 유연하게 JSON 생성
            createUser("tester", "1234", "테스터");
            MockHttpSession session = loginSession("tester", "1234");

            Map<String, Object> requestBody = Map.of(
                    "title", "테스트 제목",
                    "content", "테스트 내용"
            );

            // When & Then
            mockMvc.perform(post("/api/posts")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody))) // ObjectMapper 활용
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.boardId").exists())
                    .andExpect(jsonPath("$.title").value("테스트 제목"))
                    .andExpect(jsonPath("$.writerName").value("테스터"));
        }

        @Test
        @DisplayName("비로그인 사용자가 게시글 생성을 시도하면 401 Unauthorized를 반환한다.")
        void createPostFailUnauthenticated() throws Exception {
            Map<String, String> body = Map.of("title", "제목", "content", "내용");

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest
        @NullAndEmptySource // null, "" 케이스 자동 생성
        @ValueSource(strings = {" ", "    "}) // 공백 케이스 추가
        @DisplayName("제목이 유효하지 않은(null, 공백 등) 경우 400 Bad Request를 반환한다.")
        void createPostFailInvalidTitle(String invalidTitle) throws Exception {
            // Given
            createUser("tester", "1234", "테스터");
            MockHttpSession session = loginSession("tester", "1234");
            Map<String, String> body = Map.of("title", invalidTitle == null ? "" : invalidTitle, "content", "내용");
            // 참고: Map.of는 null value를 허용하지 않으므로 실제 null 테스트 시에는 다른 방식 사용 권장

            // When & Then
            mockMvc.perform(post("/api/posts")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("제목이 최대 허용 길이(예: 100자)를 초과하면 400 에러를 반환한다.")
        void createPostFailTitleTooLong() throws Exception {
            // 경계값 분석(Boundary Value Analysis) 적용
            String longTitle = "A".repeat(101);
            createUser("tester", "1234", "테스터");
            MockHttpSession session = loginSession("tester", "1234");
            Map<String, String> body = Map.of("title", longTitle, "content", "내용");

            mockMvc.perform(post("/api/posts")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("게시글 수정/삭제 권한 테스트")
    class AuthorityTest {

        @Test
        @DisplayName("작성자가 아닌 사용자가 수정을 시도하면 403 Forbidden을 반환하고 데이터는 변하지 않는다.")
        void updatePostFailForbidden() throws Exception {
            // Given
            createUser("writer", "1234", "작성자");
            createUser("hacker", "1234", "해커");

            MockHttpSession writerSession = loginSession("writer", "1234");
            MockHttpSession hackerSession = loginSession("hacker", "1234");

            Long boardId = createPostAndGetId(writerSession, "원본 제목", "원본 내용");
            Map<String, Object> updateBody = Map.of("boardId", boardId, "title", "해킹", "content", "해킹");

            // When: 작성자가 아닌 '해커' 세션으로 요청
            mockMvc.perform(put("/api/posts/{id}", boardId)
                            .session(hackerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateBody)))
                    .andExpect(status().isForbidden());

            // Then: 데이터 보존 확인
            mockMvc.perform(get("/api/posts/{id}", boardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("원본 제목"));
        }
    }
}