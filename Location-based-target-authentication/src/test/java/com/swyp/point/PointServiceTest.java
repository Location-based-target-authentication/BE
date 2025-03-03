package com.swyp.point;

import com.swyp.point.entity.Point;
import com.swyp.point.entity.PointHistory;
import com.swyp.point.enums.PointType;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class PointServiceTest {

    @Autowired
    private PointService pointService;

    @MockBean
    private PointRepository pointRepository;

    @MockBean
    private PointHistoryRepository pointHistoryRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testAddPoints() {
        // Given
        AuthUser mockUser = new AuthUser("1L", "mj", "jj@naver.com", "123123123", SocialType.GOOGLE);
        Mockito.when(userRepository.findByUserId(1L)).thenReturn(Optional.of(mockUser));

        Point mockPoint = new Point(mockUser);
        mockPoint.setTotalPoints(100); // 기존 포인트를 100으로 설정

        Mockito.when(pointRepository.findByAuthUser(mockUser)).thenReturn(Optional.of(mockPoint));

        // When
        pointService.addPoints(mockUser, 50, PointType.ACHIEVEMENT, "테스트 포인트 지급", 10L);

        // Then
        assertEquals(150, mockPoint.getTotalPoints()); // 포인트가 증가했는지 확인
        Mockito.verify(pointRepository, Mockito.times(1)).save(Mockito.any(Point.class)); // 한 번만 저장 확인
        Mockito.verify(pointHistoryRepository, Mockito.times(1)).save(Mockito.any(PointHistory.class)); // 이력 저장 확인
    }
}
