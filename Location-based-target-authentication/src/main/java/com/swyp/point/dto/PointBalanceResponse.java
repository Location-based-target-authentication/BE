package com.swyp.point.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointBalanceResponse {
    private Long userId;
    private String socialId;
    private int totalPoints; //현재 보유 포인트

    public PointBalanceResponse(Long userId, String socialId, int totalPoints) {
        this.userId = userId;
        this.socialId = socialId;
        this.totalPoints = totalPoints;
    }
}
