package sweep.demo.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Access Token 만료 예외 처리
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleAccessExpiredJwtException(ExpiredJwtException e) {
        System.out.println("[GlobalExceptionHandler] Access Token 만료됨!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON) // JSON 응답 보장
                .body(Map.of(
                        "error", "TOKEN_EXPIRED",
                        "message", "Access Token이 만료되었습니다. Refresh Token을 사용하세요."
                ));
    }

    // Refresh Token 만료 예외 처리
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleRefreshExpiredException(RefreshTokenExpiredException e) {
        System.out.println("[GlobalExceptionHandler] Refresh Token 만료됨!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON) // JSON 응답 보장
                .body(Map.of(
                        "error", "REFRESH_TOKEN_EXPIRED",
                        "message", "Refresh Token이 만료되었습니다. 다시 로그인하세요."
                ));
    }

    // 유효하지 않은 JWT 토큰 예외 처리
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(JwtException e) {
        System.out.println("[GlobalExceptionHandler] 유효하지 않은 JWT 토큰!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON) // JSON 응답 보장
                .body(Map.of(
                        "error", "INVALID_TOKEN",
                        "message", "유효하지 않은 Access Token입니다."
                ));
    }

    // 서버 내부 오류 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        System.out.println("[GlobalExceptionHandler] 서버 내부 오류 발생!");
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON) // JSON 응답 보장
                .body(Map.of(
                        "error", "SERVER_ERROR",
                        "message", "서버 내부 오류가 발생했습니다."
                ));
    }
}

