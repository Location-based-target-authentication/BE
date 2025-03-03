package com.swyp.social_login.repository;

import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUserId(Long userId);
    
    // findByUserId와 동일한 기능이지만 메소드 명을 다르게 하여 구분
    @Query("SELECT au FROM AuthUser au WHERE au.userId = :userId")
    Optional<AuthUser> findByUserIdEquals(@Param("userId") Long userId);

    // 소셜 로그인 플랫폼의 고유 ID로 사용자 검색
    Optional<AuthUser> findBySocialId(String socialId);
}
