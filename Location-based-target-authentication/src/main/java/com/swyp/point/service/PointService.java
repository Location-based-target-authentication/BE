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
        return pointRepository.findByAuthUser(authUser)
                .orElseGet(() -> {
                    Point newPoint = new Point(authUser);
                    return pointRepository.save(newPoint);
                });
    }

    //포인트 조회
    public int getUserPoints(AuthUser authUser) {
        Point point = getOrCreatePoint(authUser);
        return point.getTotalPoints();
    }
    //포인트 지급
    @Transactional
    public void addPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = getOrCreatePoint(authUser);
        point.addPoints(points);
        pointRepository.save(point);
        // 포인트 이력 저장
        PointHistory pointHistory = new PointHistory();
        pointHistory.setAuthUser(authUser);
        pointHistory.setPoints(points);
        pointHistory.setPointType(pointType); // ACHIEVEMENT or BONUS
        pointHistory.setDescription(description);
        pointHistory.setGoalId(goalId);
        pointHistory.setCreatedAt(LocalDateTime.now());
        pointHistoryRepository.save(pointHistory);
    }
    //포인트 차감
    @Transactional
    public boolean deductPoints(AuthUser authUser, int points, PointType pointType, String description, Long goalId){
        Point point = getOrCreatePoint(authUser);
        if(!point.subtractPoints(points)){
            return false;
        }
        pointRepository.save(point);
        pointHistoryRepository.save(new PointHistory(authUser, -points, pointType, description, goalId));
        // 메일 발송 조건 (쿠폰 지급 시)
        if (pointType == PointType.GIFT_STARBUCKS || pointType == PointType.GIFT_COUPON) {
            String giftType = pointType == PointType.GIFT_STARBUCKS ? "스타벅스" : "CU 만원 쿠폰";
            try {
                mailService.sendGiftNotification(
                        authUser.getEmail(), // 수신자 이메일
                        authUser.getUsername(), // 사용자 이름
                        authUser.getPhoneNumber(),
                        giftType                 // 쿠폰 종류
                );
            } catch (Exception e) {
                throw new RuntimeException("쿠폰 지급 이메일 전송 실패: " + e.getMessage());
            }
        }
        return true;
    }
    // 포인트 이력 조회 메서드
    public List<PointHistory> getPointHistory(Long userId) {
        return pointHistoryRepository.findByAuthUserId(userId);
    }
}
