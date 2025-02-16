package sweep.demo.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sweep.demo.global.security.JwtAuthenticationFilter;
import sweep.demo.global.security.JwtUtil;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/kakao/callback",
                                "/api/v1/auth/kakao/login",
                                "/api/v1/auth/kakao/userinfo",
                                "/api/v1/auth/google/callback",
                                "/api/v1/auth/google/login",
                                "/api/v1/auth/google/userinfo",
                                "/api/v1/auth/refresh"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

                            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
                            String socialId;

                            if ("google".equals(registrationId)) {
                                socialId = oauthUser.getAttribute("sub");
                            } else if ("kakao".equals(registrationId)) {
                                socialId = oauthUser.getAttribute("id").toString();
                            } else {
                                throw new IllegalArgumentException("지원하지 않는 OAuth 공급자: " + registrationId);
                            }
                            String jwtToken = jwtUtil.generateAccessToken(socialId);
                            response.setHeader("Authorization", "Bearer " + jwtToken);
                            response.sendRedirect("https://front.com/oauth-success?token=" + jwtToken);
                        })
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // JWT 필터를 OAuth2Login 필터보다 뒤에 추가!
        return http.build();
    }

    @Value("${cors.allowed-origins:*}") // 환경 변수에서 CORS 도메인 읽기
    private String allowedOrigins;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(","))); // 여러 개의 도메인 허용 가능
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
