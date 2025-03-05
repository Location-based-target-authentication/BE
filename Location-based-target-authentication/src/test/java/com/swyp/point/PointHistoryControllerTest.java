package com.swyp.point;

import com.swyp.TestConfig.TestSecurityConfig;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
public class PointHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private WebApplicationContext context;
    @BeforeEach
    void setup() {
        // 가짜 사용자 데이터 설정
        AuthUser mockUser = new AuthUser("1L", "mj", "jj@naver.com", "123123123", SocialType.GOOGLE);
        ReflectionTestUtils.setField(mockUser, "id", 1L); // 직접 ID 설정
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser)); // 유저가 존재하도록
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    @Test
    public void testGetPointHistory() throws Exception {
        String userId = "1"; // 테스트할 사용자 ID
        mockMvc.perform(get("/api/v1/points/history/" + Long.parseLong(userId)))
                .andDo(print());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
//                .andExpect(status().isOk()) // HTTP 200 상태 코드 확인
//                .andExpect(jsonPath("$.userId").value(1)) // 응답 JSON의 userId 확인
//                .andExpect(jsonPath("$.historyList").isArray()) // 포인트 이력 리스트가 존재하는지 확인
//                .andDo(print()); // 응답 내용 출력
    }
}