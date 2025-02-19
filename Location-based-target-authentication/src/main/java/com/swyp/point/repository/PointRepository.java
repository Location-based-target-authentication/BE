package com.swyp.point.repository;

import com.swyp.point.entity.Point;
import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByAuthUser(AuthUser authUser);
}
