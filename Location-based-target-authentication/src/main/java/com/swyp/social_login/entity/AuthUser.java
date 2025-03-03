package com.swyp.social_login.entity;
import com.swyp.point.entity.Point;
import com.swyp.point.entity.PointHistory;
import com.swyp.social_login.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String socialId; // 카카오 또는 구글에서 받은 고유 ID

    @Column(nullable = false, unique = true)
    private Long userId;

    @PrePersist
    @PreUpdate
    private void setUserIdFromId() {
        if (this.id != null) {
            this.userId = this.id;
        }
    }

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length=512)
    private String accessToken;

    @Column(length=512)
    private String refreshToken;

    @Column(nullable = false, length=100, unique = true)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING) // ENUM('GOOGLE', 'KAKAO')로 저장
    @Column(nullable = false)
    private SocialType socialType;

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Point point = null;

    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PointHistory> pointHistories = new ArrayList<>();


    @Builder
    public AuthUser(String socialId, String username, String email, String accessToken, SocialType socialType) {
        this.socialId = socialId;
        this.username = username;
        this.name = username;
        this.accessToken = accessToken;
        this.email = email;
        this.socialType = socialType;
        this.point = null;
        this.pointHistories = new ArrayList<>();
    }


    public void updatePhoneNumber(String phone) {
        this.phoneNumber = phone;
    }
}

