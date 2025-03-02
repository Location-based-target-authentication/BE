package com.swyp.point.entity;
import com.swyp.social_login.entity.AuthUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="points")
@Getter
@Setter
@NoArgsConstructor
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="user_id", referencedColumnName="user_id", nullable = false, unique = true)
    private AuthUser authUser;

    @Column(name="total_points", nullable = false)
    private int totalPoints = 2000;

    public Point(AuthUser authUser) {
        this.authUser = authUser;
        this.totalPoints = 2000;
    }
    public void addPoints(int points){
        this.totalPoints += points;
    }
    public boolean subtractPoints(int points){
        if(this.totalPoints<points){
            return false;
        }
        this.totalPoints -=points;
        return true;
    }

}
