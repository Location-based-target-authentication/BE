package com.swyp.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="name", length=20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private GoalStatus status = GoalStatus.DRAFT;

    @Column(name="start_date", nullable = false)
    private LocalDate startDate;

    @Column(name="end_date", nullable = false)
    private LocalDate endDate;

    @Column(name="location_name", length=100, nullable = false)
    private String locationName;

    @Column(name="latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name="longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name="radius", nullable = false)
    private Integer radius = 5;

    @Column(name="target_count", nullable = false)
    private Integer targetCount;

    @Column(name="achieved_count", nullable = false)
    private Integer achievedCount = 0;

    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    
    // @PrePersist 엔티티가 만들어지기 전에 자동으로 실행되는 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // @PreUpdate: 엔티티가 업데이트되기 전에 자동으로 실행되는 메서드
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
