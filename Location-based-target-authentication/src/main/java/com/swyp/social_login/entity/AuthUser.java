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

    // 이 id로 작업하고 FE에 전달
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String socialId; // 카카오 또는 구글에서 받은 고유 ID

    // userId는 id 필드와 동일한 값을 가짐
    @Column(nullable = false, unique = true)
    private Long userId;

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


    public AuthUser(String socialId, String username, String email, String accessToken, SocialType socialType) {
        this.socialId = socialId;
        this.username = username;
        this.name = username;
        this.accessToken = accessToken;
        this.email = email;
        this.socialType = socialType;
    }


    public void updatePhoneNumber(String phone) {
        this.phoneNumber = phone;
    }
}

