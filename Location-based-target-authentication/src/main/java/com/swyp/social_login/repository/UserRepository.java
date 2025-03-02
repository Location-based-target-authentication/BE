package com.swyp.social_login.repository;

import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUserId(Long userId);
    
    // findByUserId와 동일한 기능이지만 메소드 명을 다르게 하여 구분
    Optional<AuthUser> findByUserIdEquals(Long userId);
}
