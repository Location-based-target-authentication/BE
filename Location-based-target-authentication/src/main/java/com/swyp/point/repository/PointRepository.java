package com.swyp.point.repository;

import com.swyp.point.entity.Point;
import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByAuthUser(AuthUser authUser);
    
    @Query("SELECT p FROM Point p JOIN p.authUser a WHERE a.userId = :userId")
    Optional<Point> findByAuthUserUserId(@Param("userId") Long userId);
    void deleteByAuthUser(AuthUser authUser);
    
    @Modifying
    @Query("DELETE FROM Point p WHERE p.authUser.id = :id")
    void deleteByAuthUser_Id(@Param("id") Long id);
}
