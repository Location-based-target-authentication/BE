package com.swyp.users.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "private_agreement")
@Getter
@Setter
@NoArgsConstructor
public class PrivateAgreement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean agreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    public PrivateAgreement(Long userId) {
        this.userId = userId;
        this.agreed = false;
    }

    public void agree() {
        this.agreed = true;
        this.agreedAt = LocalDateTime.now();
    }
} 