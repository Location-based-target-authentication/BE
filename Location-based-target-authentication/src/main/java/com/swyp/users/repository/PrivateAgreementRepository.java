package com.swyp.users.repository;

import com.swyp.users.domain.PrivateAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PrivateAgreementRepository extends JpaRepository<PrivateAgreement, Long> {
    Optional<PrivateAgreement> findByUserId(Long userId);
} 