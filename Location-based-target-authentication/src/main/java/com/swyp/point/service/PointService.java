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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MailService mailService;
    
    /**
     * 쿠폰 정보를 담는 내부 클래스
     */
    private static class CouponInfo {
        private final String type;
        private final String code;
        private final int amount;

        public CouponInfo(String type, int amount) {
            this.type = type;
            this.code = generateCouponCode();
            this.amount = amount;
        }

        private String generateCouponCode() {
            // 실제 쿠폰 코드 생성 로직 (간단한 예시)
            return "WILLGO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    
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
        // 포인트 부족 확인
        if(!point.subtractPoints(points)){
            throw new IllegalArgumentException("포인트가 부족합니다. 현재 포인트: " + point.getTotalPoints() + ", 필요 포인트: " + points);
        }
        
        try {
            // 포인트 저장
            pointRepository.save(point);
            
            // 포인트 이력 저장
            pointHistoryRepository.save(new PointHistory(authUser, -points, pointType, description, goalId));
            
            // 쿠폰 타입인 경우 쿠폰 발행 및 이메일 발송
            if (pointType == PointType.GIFT_STARBUCKS || pointType == PointType.GIFT_COUPON) {
                processCouponIssuance(authUser, pointType, points, description);
            }
            
            return true;
        } catch (Exception e) {
            // 상세 오류 로깅
            System.err.println("포인트 차감 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("포인트 차감 중 오류 발생: " + e.getMessage(), e);
        }
    }
    
    /**
     * 쿠폰 발급 및 이메일 발송 처리
     */
    private void processCouponIssuance(AuthUser authUser, PointType pointType, int points, String description) {
        try {
            // 쿠폰 정보 생성
            CouponInfo couponInfo = createCouponInfo(pointType, points);
            
            // 사용자 정보 확인
            String userEmail = authUser.getEmail();
            String userName = authUser.getName();
            
            if (userEmail == null || userEmail.isEmpty()) {
                System.out.println("사용자 이메일 정보가 없어 쿠폰 발송을 건너뜁니다.");
                return;
            }
            
            // 쿠폰 발송 처리
            try {
                mailService.sendCouponEmail(
                    userEmail, 
                    userName, 
                    couponInfo.type, 
                    couponInfo.code, 
                    points
                );
                
                // 로그 기록
                System.out.println("쿠폰 발행 완료: " + couponInfo.type + ", 코드: " + couponInfo.code + 
                                 ", 사용자: " + userName + ", 이메일: " + userEmail);
            } catch (Exception emailEx) {
                // 이메일 전송 실패 시 로그만 남기고 예외를 표시하지 않음
                System.out.println("이메일 전송 실패했지만 포인트 차감은 정상적으로 처리됨: " + emailEx.getMessage());
            }
                               
        } catch (Exception e) {
            // 쿠폰 정보 생성 실패 시 로그만 남기고 계속 진행
            System.out.println("쿠폰 정보 생성 실패했지만 포인트 차감은 정상적으로 처리됨: " + e.getMessage());
        }
    }
    
    /**
     * 쿠폰 정보 생성
     */
    private CouponInfo createCouponInfo(PointType pointType, int points) {
        switch (pointType) {
            case GIFT_STARBUCKS:
                return new CouponInfo("스타벅스 기프티콘", points);
            case GIFT_COUPON:
                return new CouponInfo("편의점 기프티콘", points);
            default:
                throw new IllegalArgumentException("지원하지 않는 쿠폰 타입입니다: " + pointType);
        }
    }

    // 포인트 이력 조회 메서드
    public List<PointHistory> getPointHistory(Long id) {
        return pointHistoryRepository.findByAuthUser_Id(id);
    }
}
