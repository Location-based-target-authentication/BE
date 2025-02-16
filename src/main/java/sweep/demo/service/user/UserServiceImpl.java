package sweep.demo.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sweep.demo.entity.auth.AuthUser;
import sweep.demo.repository.auth.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    // Spring Security에서 사용하는 메서드 (로그인 시 호출되어 로그인이 가능한 유저인지 확인)
    @Override
    public UserDetails loadUserByUsername(String socialId) throws UsernameNotFoundException {
        AuthUser user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with socialId: " + socialId));

        return new org.springframework.security.core.userdetails.User(
                user.getSocialId(), "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

}


