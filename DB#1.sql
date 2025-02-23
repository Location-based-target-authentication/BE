CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 사용자 고유 번호
    email VARCHAR(100) NOT NULL UNIQUE,      -- 사용자 이메일
    username VARCHAR(50) NOT NULL,           -- 사용자 이름
    social_type ENUM('GOOGLE', 'KAKAO') NOT NULL,    -- 소셜 로그인 타입
    social_id VARCHAR(100) NOT NULL,         -- 소셜 서비스의 사용자 식별자
    points INT NOT NULL,                     -- 보유 포인트
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 계정 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,    -- 계정 정보일
    is_active BOOLEAN DEFAULT TRUE           -- 계정 활성화 상태
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
    target_count INT NOT NULL,             -- 목표 수행 횟수 (예: 12회)(신규)
    achieved_count INT DEFAULT 0 NOT NULL, -- 실제 수행 횟수 (신규)
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
    FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE     -- 목표 테이블 참조 (수정 : 온딜리트 캐스케이드)
);

-- 6. 목표 달성 기록 테이블
CREATE TABLE goal_achievements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 달성 기록 고유 번호
    user_id BIGINT NOT NULL,                -- 유저 ID
    goal_id BIGINT,                			-- 목표 ID (수정 NULL 허용 목표테이블 삭제를 위한)
    name VARCHAR(20) NOT NULL,              -- 목표 이름
    target_count INT NOT NULL,             -- 목표 수행 횟수 (예: 12회)(수정 달성률 계산을 위한)
    achieved_count INT DEFAULT 0 NOT NULL, -- 실제 수행 횟수 (수정: 달성률 계산을 위한)
    start_date DATE NOT NULL,               -- 목표 시작일
    end_date DATE NOT NULL,                 -- 목표 종료일
    points_earned INT NOT NULL,             -- 획득 포인트
    FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,    -- 목표 테이블 참조 (수정 ON DELETE SET NULL 추가) 
    FOREIGN KEY (user_id) REFERENCES users(id)     -- 유저 테이블 참조
);

-- 7. 포인트 이력 테이블
CREATE TABLE point_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 포인트 이력 고유 번호
    user_id BIGINT NOT NULL,                -- 사용자 ID 
    points INT NOT NULL,                    -- 포인트 변동량 (양수: 적립, 음수: 차감)
    type ENUM('WELCOME', 'ACHIEVEMENT', 'BONUS', 'GOAL_ACTIVATION', 'GIFT') NOT NULL,    -- 포인트 변동 유형 (가입 보너스, 목표 달성, 보너스, 목표 활성화, 기프트)
    description VARCHAR(200) NOT NULL,      -- 변동 사유 설명
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 포인트 변동일
    related_goal_id BIGINT,                -- 관련 목표 ID (수정 NULL 허용 목표테이블 삭제를 위한)
    FOREIGN KEY (user_id) REFERENCES users(id),    -- 사용자 테이블 참조
    FOREIGN KEY (related_goal_id) REFERENCES goals(id) ON DELETE SET NULL    -- 목표 테이블 참조 (수정 ON DELETE SET NULL 추가)
);

-- 8. 목표 달성 중복 방지를 위한 로그 테이블
CREATE TABLE goal_achievement_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    achieved_at DATE NOT NULL, -- 인증 날짜
    achieved_success BOOLEAN NOT NULL, -- 성공 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_goal_per_day UNIQUE (user_id, goal_id, achieved_at, achieved_success) -- 같은 날짜에 같은 목표에 대해 1개 이상의 기록 X 
);