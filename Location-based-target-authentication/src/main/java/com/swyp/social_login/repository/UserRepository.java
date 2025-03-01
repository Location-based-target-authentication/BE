package com.swyp.social_login.repository;


import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findBySocialId(String socialId);
    Optional<AuthUser> findById(String id);
}
