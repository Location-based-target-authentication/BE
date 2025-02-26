package com.swyp.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 정보 수정 요청")
@Getter
@NoArgsConstructor
public class UserModifyRequest {
    
    @Schema(description = "이름", example = "홍길동")
    private String name;
    
    @Schema(description = "이메일", example = "example@email.com")
    private String email;
    
    @Schema(description = "휴대전화번호", example = "01012345678")
    private String phoneNumber;
} 