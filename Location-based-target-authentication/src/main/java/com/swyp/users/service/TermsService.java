package com.swyp.users.service;

import com.swyp.users.domain.Terms;
import com.swyp.users.repository.TermsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermsService {
    
    private final TermsRepository termsRepository;

    @Transactional(readOnly = true)
    public Terms getTerms(Long userId) {
        return termsRepository.findByUserId(userId)
                .orElseGet(() -> new Terms(userId));
    }

    @Transactional
    public void agreeToTerms(Long userId) {
        Terms terms = termsRepository.findByUserId(userId)
                .orElseGet(() -> new Terms(userId));
        terms.agree();
        termsRepository.save(terms);
    }
} 