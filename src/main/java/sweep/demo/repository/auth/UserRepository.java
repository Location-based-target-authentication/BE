package sweep.demo.repository.auth;


import org.springframework.data.jpa.repository.JpaRepository;
import sweep.demo.SocialType;
import sweep.demo.entity.auth.AuthUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findBySocialId(String socialId);
    //Optional<AuthUser> findByEmailAndSocialType(String email, SocialType socialType);
}
