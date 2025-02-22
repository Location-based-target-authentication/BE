package com.swyp.point.entity;
import com.swyp.point.enums.PointType;
import com.swyp.social_login.entity.AuthUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="points_history")
@Getter
@Setter
@NoArgsConstructor
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private AuthUser authUser;

    @Column(name="points", nullable = false)
    private int points;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false)
    private PointType pointType;

    @Column(name="description", length=200)
    private String description;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JoinColumn(name="related_goal_id")
    private Long goalId;


    public PointHistory(AuthUser authUser, int points, PointType pointType, String description, Long goalId) {
        this.authUser = authUser;
        this.points = points;
        this.pointType = pointType;
        this.description = description;
        this.goalId = goalId;
        this.createdAt = LocalDateTime.now();
    }
}
