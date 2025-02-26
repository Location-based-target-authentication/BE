package com.swyp.users.service;

import com.swyp.users.domain.PrivateAgreement;
import com.swyp.users.repository.PrivateAgreementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrivateAgreementService {
    
    private final PrivateAgreementRepository privateAgreementRepository;

    @Transactional(readOnly = true)
    public PrivateAgreement getPrivateAgreement(Long userId) {
        return privateAgreementRepository.findByUserId(userId)
                .orElseGet(() -> new PrivateAgreement(userId));
    }

    @Transactional
    public void agreeToPrivateAgreement(Long userId) {
        PrivateAgreement agreement = privateAgreementRepository.findByUserId(userId)
                .orElseGet(() -> new PrivateAgreement(userId));
        agreement.agree();
        privateAgreementRepository.save(agreement);
    }
} 