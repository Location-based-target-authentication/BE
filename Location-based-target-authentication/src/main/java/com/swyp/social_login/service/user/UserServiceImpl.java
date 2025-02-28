package com.swyp.social_login.service.user;

import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    // Spring Security에서 사용하는 메서드 (로그인이 가능한 유저인지 확인)
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));
        return new org.springframework.security.core.userdetails.User(
                user.getUserId(), "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

}


