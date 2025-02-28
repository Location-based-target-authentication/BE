package com.swyp.point.repository;


import com.swyp.point.entity.PointHistory;
import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByAuthUserId(Long userId);
    @Query("SELECT SUM(p.points) FROM PointHistory p WHERE p.authUser = :authUser" )
    Integer getTotalPointsByAuthUser(@Param("authUser") AuthUser authUser);
}
