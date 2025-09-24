# Gamo Service Repository
Gamo 프로젝트 레포지토리입니다. 
## 📚 Git Convention Guide

### 📌 Commit Convention
커밋 메시지는 `[태그] 설명` 형식으로 작성합니다.  
태그는 **영어 대문자**로 작성하며, 설명은 간결하고 명확하게 작성합니다.  

| 태그 이름 | 설명 |
|-----------|------|
| FEAT      | 새로운 기능 추가 |
| FIX       | 버그 수정 |
| CHORE     | 사소한 수정 (빌드, 패키지, 환경 설정 등) |
| DOCS      | 문서 수정 |
| INIT      | 초기 설정 |
| TEST      | 테스트 코드 추가 / 리팩토링 테스트 코드 |
| RENAME    | 파일, 폴더명 수정/이동 |
| STYLE     | 코드 스타일 변경 (포맷팅, 세미콜론 등) |
| REFACTOR  | 코드 리팩토링 |

📍 예시
```bash
[FEAT] 검색 API 추가
[FIX] 로그인 시 NullPointerException 해결
[DOCS] README 배포 가이드 작성
```

### 🌿 Branch Strategy & Test Code Convention

#### 💻 GitHub Management

**Workflow: Gitflow Workflow**

- **develop** : 기능 통합 브랜치  
- **feature/** : 기능 단위 개발 브랜치  
- **hotfix/** : 급한 버그 수정 브랜치  
- **release/** : 배포 준비 브랜치  

---

#### ❗ Branch Naming Convention

- `develop`
- `feature/issue_number-도메인-httpMethod-api`
- `fix/issue_number-도메인-httpMethod-api`
- `release/version_number`
- `hotfix/issue_number-short-description`

📍 예시
feature/#3-user-post-api
fix/#12-auth-login-bug
release/v1.0.0


---

### 📍 Gitflow 규칙

- `develop` 브랜치에 직접 **commit/push 금지**  
- 작업 전 반드시 **issue 작성 후 Pull Request 연동**  
- Pull Request는 **2명 이상 코드 리뷰 후 merge**  
- 기능 개발 시:
  - `develop` → `feature/기능` 브랜치 생성
  - 기능 개발 완료 → PR 생성 → 리뷰 후 `develop` merge  

---

### 🚀 Test Code Convention

1. **given-when-then** 패턴 사용  
2. 테스트 메서드명: `메서드명_테스트상태_예상결과`  
   - 예: `giveCotton_CottonCountIs0_NotEnoughCotton`  
3. **극단적인 케이스까지 고려**해서 작성  
   - 예: 솜뭉치를 여러 개 줄 수 있다  
4. 다수의 값은 **@ParameterizedTest** 활용  

---
