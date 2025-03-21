package com.swyp.global.cache;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 소셜 로그인 요청 캐싱 서비스
 * 동일 사용자의 중복 요청을 방지하고 API 호출 제한을 관리합니다.
 */
@Service
public class AuthRequestCacheService {
    private static final Logger log = LoggerFactory.getLogger(AuthRequestCacheService.class);
    
    // 인증 코드를 키로, 액세스 토큰을 값으로 저장하는 캐시
    private final Map<String, String> codeToAccessTokenCache = new ConcurrentHashMap<>();
    
    // 사용자 ID를 키로, 액세스 토큰을 값으로 저장하는 캐시
    private final Map<String, String> userIdToAccessTokenCache = new ConcurrentHashMap<>();
    
    // 캐시 만료 정보 저장
    private final Map<String, Long> cacheExpiryTimes = new ConcurrentHashMap<>();
    
    // 캐시 유효 시간 (밀리초 단위, 기본 10분)
    private final long CACHE_EXPIRY_MS = TimeUnit.MINUTES.toMillis(10);

    /**
     * 인증 코드로 액세스 토큰 조회
     * @param code 인증 코드
     * @return 캐시된 액세스 토큰 또는 null
     */
    public String getAccessTokenByCode(String code) {
        cleanExpiredEntries();
        return codeToAccessTokenCache.get(code);
    }

    /**
     * 사용자 ID로 액세스 토큰 조회
     * @param userId 사용자 ID
     * @return 캐시된 액세스 토큰 또는 null
     */
    public String getAccessTokenByUserId(String userId) {
        cleanExpiredEntries();
        return userIdToAccessTokenCache.get(userId);
    }

    /**
     * 인증 코드와 액세스 토큰을 캐시에 저장
     * @param code 인증 코드
     * @param accessToken 액세스 토큰
     */
    public void cacheAccessTokenByCode(String code, String accessToken) {
        if (code != null && accessToken != null) {
            codeToAccessTokenCache.put(code, accessToken);
            cacheExpiryTimes.put("code:" + code, System.currentTimeMillis() + CACHE_EXPIRY_MS);
            log.info("인증 코드 {} 에 대한 액세스 토큰이 캐시되었습니다.", code);
        }
    }

    /**
     * 사용자 ID와 액세스 토큰을 캐시에 저장
     * @param userId 사용자 ID
     * @param accessToken 액세스 토큰
     */
    public void cacheAccessTokenByUserId(String userId, String accessToken) {
        if (userId != null && accessToken != null) {
            userIdToAccessTokenCache.put(userId, accessToken);
            cacheExpiryTimes.put("userId:" + userId, System.currentTimeMillis() + CACHE_EXPIRY_MS);
            log.info("사용자 ID {} 에 대한 액세스 토큰이 캐시되었습니다.", userId);
        }
    }

    /**
     * 사용자 ID와 인증 코드, 액세스 토큰을 모두 캐시에 저장
     * @param userId 사용자 ID
     * @param code 인증 코드 (있는 경우)
     * @param accessToken 액세스 토큰
     */
    public void cacheUserAuthInfo(String userId, String code, String accessToken) {
        cacheAccessTokenByUserId(userId, accessToken);
        
        if (code != null) {
            cacheAccessTokenByCode(code, accessToken);
        }
    }

    /**
     * 만료된 캐시 항목 정리
     */
    private void cleanExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        // 만료된 인증 코드 캐시 정리
        cacheExpiryTimes.entrySet().removeIf(entry -> {
            if (entry.getValue() < currentTime) {
                String key = entry.getKey();
                if (key.startsWith("code:")) {
                    String code = key.substring(5);
                    codeToAccessTokenCache.remove(code);
                } else if (key.startsWith("userId:")) {
                    String userId = key.substring(7);
                    userIdToAccessTokenCache.remove(userId);
                }
                return true;
            }
            return false;
        });
    }
} 