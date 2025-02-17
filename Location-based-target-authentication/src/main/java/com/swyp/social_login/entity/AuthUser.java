package com.swyp.social_login.entity;
import com.swyp.social_login.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

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

    public AuthUser(String socialId, String username, String email, String accessToken, SocialType socialType) {
        this.socialId = socialId;
        this.username = username;
        this.email = email;
        this.accessToken = accessToken;
        this.socialType = socialType;
    }
    public void updatePhoneNumber(String phone){
        this.phoneNumber = phoneNumber;
    }
}

