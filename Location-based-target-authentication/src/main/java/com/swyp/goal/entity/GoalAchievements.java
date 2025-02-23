package com.swyp.goal.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "goal_achievements")
@NoArgsConstructor
public class GoalAchievements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 달성 기록 고유 번호

    @Column(name = "user_id", nullable = false)
    private Long userId; // 유저 ID

    @Column(name = "goal_id")
    private Long goalId; // 목표 ID (NULL 허용)

    @Column(name = "name", nullable = false)
    private String name; // 목표 이름

    @Column(name = "target_count", nullable = false)
    private int targetCount; // 목표 수행 횟수

    @Column(name = "achieved_count", nullable = false)
    private int achievedCount = 0; // 실제 수행 횟수 (기본값 0)

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 목표 시작일

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 목표 종료일

    @Column(name = "points_earned", nullable = false)
    private int pointsEarned; // 획득 포인트


}
