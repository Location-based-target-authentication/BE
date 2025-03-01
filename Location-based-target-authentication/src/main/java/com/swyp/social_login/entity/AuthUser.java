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
@Builder
@Table(name = "users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String socialId; // 카카오 또는 구글에서 받은 고유 ID

    @Column(nullable = false)
    private String username;

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

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    private Point point;

    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PointHistory> pointHistories = new ArrayList<>();

    public AuthUser(Long id, String socialId, String username, String accessToken, String email, SocialType socialType) {
        this.id = id;
        this.socialId = socialId;
        this.username = username;
        this.accessToken = accessToken;
        this.email = email;
        this.socialType = socialType;
    }

    public void updatePhoneNumber(String phone){
        this.phoneNumber = phoneNumber;
    }
}

