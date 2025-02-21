package com.swyp.social_login.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoneRequestDto {
    @NotBlank(message = "전화번호는 필수 입력값임")
    @Pattern(regexp = "^(01[0-9])-([0-9]{3,4})-([0-9]{4})$", message = "유효하지 않은 전화번호 형식입니다. 예시) 010-1234-5678")
    private String phoneNumber;
}

