# 🧑‍🧑‍🧒🙋📞 GAMO SERVICE

안녕하세요. 시니어 맞춤 가족 소통 서비스 가모 Backend & Frontend 통합 레포지토리입니다. 


[2025 성신여자대학교 융합캡스톤디자인 프로젝트]

<div align="center">
<img src="https://github.com/user-attachments/assets/a164e657-8ca5-4b80-bfe4-2494e8a5e32d" width="70%" />
</div>

### 🤦‍♀️ 팀원 소개

<div align="center">

|                          Team Member                          |                               Team Member                              |                          Team Member                         |                             Team Member                             |
| :-----------------------------------------------------------: | :--------------------------------------------------------------------: | :----------------------------------------------------------: | :-----------------------------------------------------------------: |
|     <img src="https://github.com/yjhss.png" width="150" />    |       <img src="https://github.com/JiwonLee42.png" width="150" />      | <img src="https://github.com/chaehyeon02.png" width="150" /> |       <img src="https://github.com/1siis1.png" width="150" />       |
| [홍유진](https://github.com/yjhss)<br />편지 퍼블리싱, STT·SSE, 편지 API | [이지원](https://github.com/JiwonLee42)<br />영상통화·기록 UI 및 API, 녹음·STT API |  [이채현](https://github.com/chaehyeon02)<br />앨범 퍼블리싱, 앨범 API  | [김시영](https://github.com/1siis1)<br />회원·가족 UI, 소셜 로그인·JWT, FE 레이아웃 |

</div>

## 📚 목차

1. [프로젝트 소개](#프로젝트-소개)
2. [기술 스택](#-팀원-소개)
3. [서비스 아키텍처](#-서비스-아키텍처)
4. [프로젝트 구조](#프로젝트-구조)
5. [Git 협업 규칙](#-gitflow-규칙)

## 🛠 기술 스택

### Front-end
<img src="https://skillicons.dev/icons?i=javascript,html,css,tailwindcss&theme=light" height="50">  
<img src="https://skillicons.dev/icons?i=webrtc&theme=light" height="50">

### Infra & Back-end
<img src="https://skillicons.dev/icons?i=spring,mysql,nginx,jenkins,gcp&theme=light" height="50">  

| 구분            | 기술                                                  |
| ------------- | --------------------------------------------------- |
| **Language**  | Java                                                |
| **Framework** | Spring Boot                                         |
| **Database**  | MySQL, JPA                                          |
| **Infra**     | GCP, Nginx                                          |
| **CI/CD**     | Jenkins                                             |
| **Auth**      | JWT, Spring Security                                |
| **기타**        | OAuth, WebSocket, SSE, WebRTC, Google Cloud Storage |
| **외부 API**    | Google Speech To Text, Gemini API                   |
| **Frontend**  | TailwindCSS, HTML, Javascript, Thymeleaf            |

## ⛏️ 서비스 아키텍쳐

<div align="center">
<img width="90%" src="https://github.com/user-attachments/assets/9a8ad650-43c2-4543-b232-ac1fb1d32179" />
</div>

## 📁 프로젝트 구조

```
src
├── frontend/                  
│   ├── main.css               
│   ├── node_modules/          
│   └── 각종 패키지별 서브 모듈
├── java/                      
│   └── gamo/web/
│       ├── WebApplication.java 
│       ├── auth/              
│       ├── common/            
│       ├── family/            
│       ├── home/              
│       ├── letter/            
│       ├── member/            
│       ├── photo/             
│       └── videocall/         
├── resources/                 
│   ├── application.yml         
│   ├── application-secret.properties 
│   ├── google-service-account.json   
│   ├── static/                
│   └── templates/             
│       ├── fragments/         
│       ├── login.html
│       └── pages/             
├── package.json              
├── package-lock.json          
├── postcss.config.js          
└── tailwind.config.js         
```

## 📍 Git 협업 규칙

* `develop` 브랜치에 직접 **commit/push 금지**
* 작업 전 반드시 **issue 작성 후 Pull Request 연동**
* Pull Request는 **2명 이상 코드 리뷰 후 merge**
* 기능 개발 시:

  * `develop` → `feature/기능` 브랜치 생성
  * 기능 개발 완료 → PR 생성 → 리뷰 후 `develop` merge
