package com.swyp.users.repository;

import com.swyp.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserManagementRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authUser WHERE u.userId = :userId")
    Optional<User> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authUser WHERE u.id = :id")
    Optional<User> findByIdWithAuthUser(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authUser WHERE u.email = :email")
    Optional<User> findByEmailWithAuthUser(@Param("email") String email);
    
    boolean existsByUserId(Long userId);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.privacyAgreement = true AND u.termsAgreement = true")
    List<User> findAllWithAgreements();
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId AND u.privacyAgreement = true AND u.termsAgreement = true")
    boolean hasUserAcceptedAllAgreements(@Param("userId") Long userId);
} 