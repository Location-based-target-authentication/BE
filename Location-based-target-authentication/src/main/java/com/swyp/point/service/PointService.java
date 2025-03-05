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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MailService mailService;
    @Transactional
    public Point getOrCreatePoint(AuthUser authUser) {
        return pointRepository.findByAuthUserUserId(authUser.getId())
                .orElseGet(() ->
                    new Point(authUser));
//                    Point newPoint = new Point(authUser);
//                    newPoint.addPoints(2000); // 초기 포인트 지급
//                    return pointRepository.save(newPoint);

    }

    //포인트 조회
    public int getUserPoints(AuthUser authUser) {
        Point point = getOrCreatePoint(authUser);
        return point.getTotalPoints();
    }

    public int getTotalPoints(AuthUser authUser) {
            Integer totalPoints = pointHistoryRepository.getTotalPointsByAuthUser(authUser);
            return totalPoints != null ? totalPoints : 0; // 포인트가 없으면 0 반환
        }


    //포인트 지급
    @Transactional
    public void addPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = getOrCreatePoint(authUser);
        pointRepository.save(point);
        // 포인트 이력 저장
        pointHistoryRepository.save(new PointHistory(authUser, points, pointType, description, goalId));
    }
    //포인트 차감
    @Transactional
    public boolean deductPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = getOrCreatePoint(authUser);
        if(!point.subtractPoints(points)){
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        try {
            pointRepository.save(point);
            pointHistoryRepository.save(new PointHistory(authUser, -points, pointType, description, goalId));
            
            // 쿠폰 지급은 별도의 API를 통해서만 가능하도록 수정
            if (pointType == PointType.GIFT_STARBUCKS || pointType == PointType.GIFT_COUPON) {
                throw new IllegalArgumentException("쿠폰 지급은 별도의 API를 통해서만 가능합니다.");
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("포인트 차감 중 오류 발생", e);
        }
    }
    // 포인트 이력 조회 메서드
    public List<PointHistory> getPointHistory(Long id) {
        return pointHistoryRepository.findByAuthUser_Id(id);
    }
}
