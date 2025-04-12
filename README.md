# WillGO - 위치기반 목표 달성 서비스

## 📑 목차
- [프로젝트 소개](#project-intro)
- [개요](#overview)
- [주요 기능](#main-features)
- [화면 구성](#screens)
- [기술 스택](#tech-stack)
- [주요 구현 기능](#implementations)
- [시작하기](#getting-started)
- [API 문서](#api-docs)
- [지원](#support)

## 📝 프로젝트 소개 <a id="project-intro"></a>
WillGO는 위치 기반의 목표 달성을 돕는 서비스입니다. 사용자가 설정한 목표 장소에 방문하여 GPS를 통해 자동으로 인증하고 포인트 시스템과 푸시 알림으로 동기부여를 강화하는 위치기반 목표인증 서비스입니다.

## 📊 개요 <a id="overview"></a>

### 🏷️ 프로젝트 정보
| 항목 | 내용 |
|------|------|
| 프로젝트 이름 | WillGO - 위치기반 목표 달성 서비스 |
| 개발 기간 | 2024.12 - 2025.03 |
| 서비스 링크 | [WillGO 바로가기](https://locationcheckgo.netlify.app/) |
| 팀 명 | SWYP 8기 6팀 |

### 👥 팀원 구성
| 역할 | 이름 |
|------|------|
| PM | 윤현수, 박수현 |
| 디자이너 | 윤재호 |
| Backend | 김준현, 장민지, 하지혁 |
| Frontend | 박민형, 이영주 |

### 💻 Backend 개발 담당
| 개발자 | 담당 업무 |
|--------|-----------|
| 김준형 | 위치 API, 사용자 및 설정 관련 API, Swagger를 통한 API 문서화, 배포 환경 및 DB 구축 |
| 장민지 | 로그인 API, 포인트 API |
| 하지혁 | 목표 인증 API |

## 🚀 주요 기능 <a id="main-features"></a>
### 1. 목표 관리
- 목표명, 목표기간, 목표요일 설정
- 종 3개의 목표 생성 가능
- 목표 종류:
  - 인증가능 목표
  - 인증완료 목표
  - 임시저장 목표

### 2. 위치 기반 인증
- 지도 UI를 통한 내위치, 목표장소 확인
- 목표장소 100m 이내 접근 시 인증 가능
- 인증 요일, 주 인증횟수에 따라 보상포인트 차등 지급
- 주 인증횟수 채울 시 보너스포인트 추가 지급

### 3. 포인트 시스템
- 회원가입 시: 2,000p 초기 지급
- 목표 생성 시: 200p 차감
- 리워드 신청: 
  - 커피쿠폰(5,000p)
  - 편의점쿠폰(10,000p)

## 📱 화면 구성 <a id="screens"></a>

<div align="center">
  <h3>로그인 화면</h3>
  <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/login.jpg" width="200" alt="로그인 화면"/>
  
  <h3>메인 화면</h3>
  <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/main.jpg" width="200" alt="메인 화면"/>
  
  <h3>설정 화면</h3>
  <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/main_2.jpeg" width="200" alt="설정 화면"/>
  
  <h3>목표 관리</h3>
  <div style="display: flex; justify-content: center; gap: 20px;">
    <div>
      <h5>목표 추가</h5>
      <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/goal_1.jpg" width="200" alt="목표 추가"/>
    </div>
    <div>
      <h5>목표 완료</h5>
      <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/goal_2.jpg" width="200" alt="목표 완료"/>
    </div>
    <div>
      <h5>목표 확인</h5>
      <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/goal_3.jpg" width="200" alt="목표 확인"/>
    </div>
  </div>
  
  <h3>목표 위치 인증</h3>
  <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/location.png" width="200" alt="위치 인증"/>
  
  <h3>리워드 신청</h3>
  <img src="https://github.com/Location-based-target-authentication/BE/blob/df729087342116a68baeacba34b9a74fdecb33e4/Location-based-target-authentication/src/main/resources/img/point.png" width="200" alt="리워드 신청"/>
</div>

## 🔧 기술 스택 <a id="tech-stack"></a>
### Backend
- Java 17
- Spring Boot 3.2.1
- Spring Security
- JWT
- JPA/Hibernate
- MySQL
- Swagger

### Frontend
- React
- TypeScript
- shadcn/ui

### 인증
- OAuth2.0 (Google, Kakao)
- JWT 기반 인증

### 외부 API
- Kakao Maps API (위치 기반 서비스)

### DevOps
- Oracle Server
- Ubuntu
- Github Actions (CI/CD)

## 🛠️ 주요 구현 기능 <a id="implementations"></a>

### 1. 카카오맵 API 연동
- 목표위치 장소 검색 기능
- 사용자 반경 100m 이내 목표 장소 확인 로직

### 2. 사용자 관련 기능
- 소셜 로그인 (Google, Kakao)
- 서비스 약관 동의
- 개인정보 처리방침
- 회원 정보 관리
- 로그아웃 및 회원 탈퇴

### 3. API 문서화
- Swagger를 활용한 API 명세서 작성
- API 엔드포인트 및 요청/응답 데이터 구조 정의
- API 테스트 환경 구축

### 4. 보안 강화
- Spring Security를 활용한 백엔드 보안 구현
- CORS 설정을 통한 안전한 API 통신
- 민감한 사용자 정보 암호화 처리

## 🚀 시작하기 <a id="getting-started"></a>

### 요구사항
- Java 17 이상
- MySQL 8.0 이상

### 설치 및 실행 방법

#### 백엔드 설정
1. 프로젝트 클론
```bash
git clone https://github.com/Location-based-target-authentication/BE.git
cd BE/Location-based-target-authentication
```

2. 프로젝트 빌드 및 실행
```bash
# 의존성 설치
./mvnw clean install

# 애플리케이션 실행
./gradlew bootRun
```

## 📚 API 문서 <a id="api-docs"></a>

### 주요 API

#### 🔐 인증 관련 API
- `POST /api/v1/auth/google/login`: Google 소셜 로그인
- `POST /api/v1/auth/kakao/login`: Kakao 소셜 로그인
- `POST /api/v1/auth/logout`: 로그아웃
- `DELETE /api/v1/auth/withdrawal`: 회원 탈퇴

#### 🎯 목표 관리 API
- `POST /api/v1/goals`: 새로운 목표 생성
- `GET /api/v1/goals`: 목표 목록 조회
- `GET /api/v1/goals/{goalId}`: 특정 목표 상세 조회
- `PUT /api/v1/goals/{goalId}`: 목표 정보 수정
- `DELETE /api/v1/goals/{goalId}`: 목표 삭제
- `POST /api/v1/goals/{goalId}/verify`: 목표 달성 인증
- `GET /api/v1/goals/available-slots`: 사용 가능한 목표 슬롯 조회
- `GET /api/v1/goals/statistics`: 목표 달성 통계 조회
- `POST /api/v1/goals/temporary`: 임시 목표 저장
- `GET /api/v1/goals/verification-history`: 목표 인증 히스토리 조회

#### 💰 포인트 관리 API
- `GET /api/v1/points`: 포인트 내역 조회
- `GET /api/v1/points/balance`: 현재 포인트 잔액 조회
- `POST /api/v1/points/rewards`: 리워드 신청
- `GET /api/v1/points/rewards/history`: 리워드 신청 내역 조회

#### 👤 사용자 관리 API
- `GET /api/v1/users/me`: 내 정보 조회
- `PUT /api/v1/users/me`: 내 정보 수정
- `GET /api/v1/users/me/goals`: 내 목표 목록 조회
- `GET /api/v1/users/me/points`: 내 포인트 내역 조회

#### 📍 위치 관련 API
- `POST /api/v1/locations/verify`: 현재 위치 인증
- `GET /api/v1/locations/search`: 장소 검색
- `GET /api/v1/locations/nearby-goals`: 주변 목표 장소 조회
- `POST /api/v1/locations/validate`: 목표 장소 유효성 검증

#### ⚙️ 시스템 API
- `GET /api/v1/system/version`: 앱 버전 정보 조회
- `GET /api/v1/system/maintenance`: 시스템 점검 정보 조회
- `GET /api/v1/system/terms`: 서비스 약관 조회
- `GET /api/v1/system/privacy-policy`: 개인정보 처리방침 조회

## 📧 지원 <a id="support"></a>

[![Gmail Badge](https://img.shields.io/badge/Gmail-d14836?style=for-the-badge&logo=Gmail&logoColor=white&link=mailto:kjunh972@gmail.com)](mailto:kjunh972@gmail.com)