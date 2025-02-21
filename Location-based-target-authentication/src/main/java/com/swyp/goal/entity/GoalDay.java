package com.swyp.goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "goal_days")
@NoArgsConstructor
public class GoalDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 반복 요일 고유 번호

    @Column(name = "goal_id", nullable = false)
    private Long goalId; // 목표 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; // 요일


}