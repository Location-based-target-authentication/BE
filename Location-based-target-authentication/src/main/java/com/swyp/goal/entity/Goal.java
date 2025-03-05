package com.swyp.goal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.swyp.social_login.entity.AuthUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "목표 ID", example = "1")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    @Schema(description = "사용자 ID (외래 키)", example = "101")
    private Long userId;

    @Column(name = "auth_user_id", nullable = false)
    @Schema(description = "인증 사용자 ID", example = "101")
    private Long authUserId;
    
    @Column(name = "name", length = 20, nullable = false)
    @Schema(description = "목표 이름", example = "매일 아침 조깅")
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('DRAFT', 'ACTIVE', 'COMPLETE') DEFAULT 'DRAFT'")
    @Schema(description = "목표 상태 (DRAFT, ACTIVE, COMPLETE)", example = "ACTIVE")
    private GoalStatus status = GoalStatus.DRAFT;
    
    @Column(name = "start_date", nullable = false)
    @Schema(description = "목표 시작 날짜", example = "2025-03-01")
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    @Schema(description = "목표 종료 날짜", example = "2025-06-01")
    private LocalDate endDate;
    
    @Column(name = "location_name", length = 100, nullable = false)
    @Schema(description = "목표 장소 이름", example = "한강공원")
    private String locationName;
    
    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;
    
    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;
    
    @Column(name = "radius", nullable = false, columnDefinition = "INT DEFAULT 100")
    @Schema(description = "목표 반경 (단위: m)", example = "100")
    private Integer radius = 100;
    
    @Column(name = "target_count", nullable = false)
    @Schema(description = "목표 설정 횟수", example = "10")
    private Integer targetCount;
    
    @Column(name = "achieved_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Schema(description = "목표 달성 횟수", example = "3")
    private Integer achievedCount = 0;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Schema(description = "목표 생성 시간", example = "2025-02-22T10:15:30")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @Schema(description = "목표 수정 시간", example = "2025-02-22T12:00:00")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_user_id", insertable = false, updatable = false)ㅎ
    private AuthUser authUser;
    
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalDay> goalDays = new ArrayList<>();

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalAchievementsLog> goalAchievementsLogs = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
