package com.swyp.point.service;
import com.swyp.point.entity.Point;
import com.swyp.point.entity.PointHistory;
import com.swyp.point.enums.PointType;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.social_login.entity.AuthUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    //포인트 조회
    public int getUserPoints(AuthUser authUser) {
        return pointRepository.findByAuthUser(authUser)
                .map(Point::getTotalPoints)
                .orElseThrow(()->new IllegalArgumentException("포인트 정보가 없음"));
    }
    //포인트 지급
    @Transactional
    public void addPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = pointRepository.findByAuthUser(authUser).orElseGet(()->pointRepository.save(new Point(authUser)));
        point.addPoints(points);
        pointRepository.save(point);
        pointHistoryRepository.save(new PointHistory(authUser, points, pointType, description, goalId));
    }
    //포인트 차감
    @Transactional
    public boolean deductPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = pointRepository.findByAuthUser(authUser).orElseGet(()->pointRepository.save(new Point(authUser)));
        if(!point.subtractPoints(points)){
            return false;
        }
        pointRepository.save(point);
        pointHistoryRepository.save(new PointHistory(authUser, -points, pointType, description, goalId));
        return true;
    }
    // 포인트 이력 조회 메서드
    public List<PointHistory> getPointHistory(Long userId) {
        return pointHistoryRepository.findByAuthUserId(userId);
    }
}
