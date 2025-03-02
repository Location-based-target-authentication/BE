package com.swyp.users.domain;

import com.swyp.social_login.entity.AuthUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phoneNumber;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "privacy_agreement", nullable = false, columnDefinition = "boolean default false")
    private boolean privacyAgreement = false;

    @Column(name = "privacy_agreement_at")
    private LocalDateTime privacyAgreementAt;

    @Column(name = "terms_agreement", nullable = false, columnDefinition = "boolean default false")
    private boolean termsAgreement = false;

    @Column(name = "terms_agreement_at")
    private LocalDateTime termsAgreementAt;

    @OneToOne
    @JoinColumn(name = "auth_user_id")
    private AuthUser authUser;
} 