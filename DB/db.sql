-- 1. 사용자 테이블
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,   -- 사용자 ID (자동 증가)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 계정 생성일
    email VARCHAR(255) NOT NULL UNIQUE,             -- 이메일 (고유 값)
    last_login_at DATETIME(6) NULL,                 -- 마지막 로그인 시간
    name VARCHAR(255) NOT NULL,                     -- 사용자 이름
    phone_number VARCHAR(20) NULL,                  -- 전화번호 (NULL 허용)
    access_token VARCHAR(512) NULL,                 -- 액세스 토큰 (NULL 허용)
    refresh_token VARCHAR(512) NULL,                -- 리프레시 토큰 (NULL 허용)
    social_id VARCHAR(255) NULL UNIQUE,             -- 소셜 로그인 ID (고유 값)
    social_type ENUM('GOOGLE', 'KAKAO') NULL,       -- 소셜 로그인 유형 (Google, Kakao)
    username VARCHAR(255) NOT NULL,                 -- 사용자명
    user_id VARCHAR(255) NOT NULL UNIQUE,           -- 사용자 ID (고유 값)
    privacy_agree TINYINT(1) NOT NULL DEFAULT 0,    -- 개인정보 동의 여부 (0: 미동의, 1: 동의)
    privacy_agree_at DATETIME(6) NULL,              -- 개인정보 동의 일시
    terms_agree TINYINT(1) NOT NULL DEFAULT 0,      -- 이용약관 동의 여부 (0: 미동의, 1: 동의)
    terms_agree_at DATETIME(6) NULL                 -- 이용약관 동의 일시
);


-- 2. 약관 테이블
CREATE TABLE terms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 약관 고유 번호
    type ENUM('SERVICE', 'PRIVACY') NOT NULL,    -- 약관 유형 (서비스/개인정보)
    content TEXT NOT NULL,                   -- 약관 내용
    version VARCHAR(10) NOT NULL,            -- 약관 버전
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP    -- 약관 생성일시
);

-- 3. 사용자 약관 동의 테이블
CREATE TABLE user_terms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 약관 동의 고유 번호
    user_id BIGINT NOT NULL,                -- 사용자 ID 
    term_id BIGINT NOT NULL,                -- 약관 ID 
    is_agreed BOOLEAN DEFAULT FALSE,         -- 동의 여부
    agreed_at TIMESTAMP,                     -- 동의 일시
    FOREIGN KEY (user_id) REFERENCES users(id),    -- 사용자 테이블 참조
    FOREIGN KEY (term_id) REFERENCES terms(id)    -- 약관 테이블 참조
);

-- 4. 목표 테이블
CREATE TABLE goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 목표 고유 번호
    user_id BIGINT NOT NULL,                -- 사용자 ID 
    name VARCHAR(20) NOT NULL,              -- 목표 이름
    status ENUM('DRAFT', 'ACTIVE', 'COMPLETE') DEFAULT 'DRAFT',    -- 목표 상태 (임시저장/진행중/성공)
    start_date DATE NOT NULL,               -- 목표 시작일
    end_date DATE NOT NULL,                 -- 목표 종료일
    location_name VARCHAR(100) NOT NULL,    -- 목표 장소명
    target_count INT NOT NULL,             -- 목표 수행 횟수 (예: 12회) 추가ㅁㅁ
    achieved_count INT DEFAULT 0 NOT NULL, -- 실제 수행 횟수 추가ㅁㅁ
    latitude DECIMAL(10, 8) NOT NULL,       -- 위도
    longitude DECIMAL(11, 8) NOT NULL,      -- 경도
    radius INT DEFAULT 5 NOT NULL,          -- 인증 반경 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 목표 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,    -- 목표 수정일
    FOREIGN KEY (user_id) REFERENCES users(id)    -- 사용자 테이블 참조
);

-- 5. 목표 반복 요일 테이블
CREATE TABLE goal_days (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 반복 요일 고유 번호
    goal_id BIGINT NOT NULL,                -- 목표 ID 
    day_of_week ENUM('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN') NOT NULL,    -- 요일
    FOREIGN KEY (goal_id) REFERENCES goals(id)    -- 목표 테이블 참조
);

-- 6. 목표 달성 기록 테이블
CREATE TABLE goal_achievements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 달성 기록 고유 번호
    goal_id BIGINT NOT NULL,                -- 목표 ID 
    achieved_at TIMESTAMP NOT NULL,         -- 달성 일시
    points_earned INT NOT NULL,             -- 획득 포인트
    is_scheduled_day BOOLEAN NOT NULL,      -- 예정된 요일 여부
    latitude DECIMAL(10, 8) NOT NULL,       -- 인증 위치 위도
    longitude DECIMAL(11, 8) NOT NULL,      -- 인증 위치 경도
    FOREIGN KEY (goal_id) REFERENCES goals(id)    -- 목표 테이블 참조
);

-- 7. 포인트 history 테이블
CREATE TABLE point_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,  -- 포인트 이력 ID (자동 증가)
    created_at DATETIME(6) NOT NULL,               -- 포인트 변동 시간
    description VARCHAR(200) NULL,                 -- 변동 사유 (NULL 허용)
    goal_id BIGINT NULL,                           -- 관련 목표 ID (NULL 허용)
    type ENUM('WELCOME', 'ACHIEVEMENT', 'BONUS', 'GOAL_ACTIVATION', 'GIFT') NOT NULL, -- 포인트 변동 유형
    points INT NOT NULL,                           -- 변동된 포인트 값 (양수: 적립, 음수: 차감)
    user_id BIGINT NOT NULL                        -- 사용자 ID (NULL 허용 안됨)
);

-- 8. 포인트 테이블
CREATE TABLE points (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,  -- 고유 ID (자동 증가)
    total_points INT NOT NULL,                      -- 총 포인트
    user_id BIGINT NOT NULL UNIQUE                 -- 사용자 ID (고유 값)
);
