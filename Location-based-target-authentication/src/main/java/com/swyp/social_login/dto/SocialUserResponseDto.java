package com.swyp.social_login.dto;

import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class SocialUserResponseDto {
    private Long id;
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private SocialType socialType;
    private String username;
    private String email;
    private String phoneNumber;

    public SocialUserResponseDto(AuthUser authUser) {
        this.id = authUser.getId();
        // 소셜 로그인 제공자의 userId 사용
        this.userId = authUser.getUserId();
        this.accessToken = authUser.getAccessToken();
        this.refreshToken = authUser.getRefreshToken();
        this.socialType = authUser.getSocialType();
        this.username = authUser.getUsername();
        this.email = authUser.getEmail();
        this.phoneNumber = authUser.getPhoneNumber();
    }

    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
