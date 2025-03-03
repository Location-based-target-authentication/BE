package com.swyp.users.dto;

import com.swyp.social_login.enums.SocialType;
import com.swyp.users.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private SocialType socialType;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean privacyAgreement;
    private LocalDateTime privacyAgreementAt;
    private boolean termsAgreement;
    private LocalDateTime termsAgreementAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.privacyAgreement = user.isPrivacyAgreement();
        this.privacyAgreementAt = user.getPrivacyAgreementAt();
        this.termsAgreement = user.isTermsAgreement();
        this.termsAgreementAt = user.getTermsAgreementAt();
        
        if (user.getAuthUser() != null) {
            this.socialType = user.getAuthUser().getSocialType();
            this.username = user.getAuthUser().getUsername();
        }
    }
}
