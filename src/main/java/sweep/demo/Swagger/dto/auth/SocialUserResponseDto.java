package sweep.demo.Swagger.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import sweep.demo.SocialType;
import sweep.demo.entity.auth.AuthUser;


@Getter
@NoArgsConstructor
public class SocialUserResponseDto {
    private Long id;
    private String socialId;
    private String accessToken;
    private String refreshToken;
    private SocialType socialType;

    private String username;
    private String email;
    private String phoneNumber;

    public SocialUserResponseDto(AuthUser authUser) {
        this.id = authUser.getId();
        this.socialId = authUser.getSocialId();
        this.accessToken = authUser.getAccessToken();
        this.refreshToken = authUser.getRefreshToken();
        this.socialType = authUser.getSocialType();
        this.username = authUser.getUsername();
        this.email = authUser.getEmail();
        this.phoneNumber = authUser.getPhoneNumber();
    }

    public void setTokens(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
