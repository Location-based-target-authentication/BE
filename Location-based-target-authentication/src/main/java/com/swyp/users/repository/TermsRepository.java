package com.swyp.users.repository;

import com.swyp.users.domain.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {
    Optional<Terms> findByUserId(Long userId);
} 