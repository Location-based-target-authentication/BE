package com.swyp.users.dto;

import com.swyp.social_login.enums.SocialType;
import com.swyp.users.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private SocialType socialType;

    public UserResponse(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.socialType = user.getAuthUser().getSocialType();
    }
}
