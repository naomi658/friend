## 🧑‍🤝‍🧑 apr-backend-assignment
안녕하세요. APR 백엔드 직무에 지원한 **이민영**입니다.<br/>
읽어주심에 감사드리며, 향후 좋은 인연이 되면 좋겠습니다.
<br/>
## 🚀 프로젝트 개요
| **항목** | **내용** |
|------|------|
| **Framework** | Spring Boot 3.3.4 |
| **Language** | Java 21 |
| **Build** Tool | Gradle |
| **DB** | H2 (In-Memory) |
| **API Docs** | Swagger |
| **ORM** | Spring Data JPA |
<br/>

## 📁 프로젝트 구조(MVC)
src/<br/>
├── main/<br/>
│ ├── java/com/apr/backend/friend/<br/>
│ │ ├── controller/ # API Controller<br/>
│ │ ├── dto/ # DTO<br/>
│ │ ├── entity/ # Entity<br/>
│ │ ├── repository/ # JPA Repository<br/>
│ │ ├── service/ # Service Logic<br/>
│ │ └── AprFriendApplication.java<br/>
│ └── resources/<br/>
│   ├── application.properties<br/>
<br/>
## 🗄️ H2 Database Console
| 항목 | 값 |
|------|------|
| **H2 Console URL** | http://localhost:8008/AprFriend/h2-console
| **JDBC URL** | jdbc:h2:mem:frienddb |
| **Username** | sa |
| **Password** | (빈값) |<br/>
<br/>

## ⚙️ 테스트 방법
1️⃣ 서버 실행

1. Gradle 빌드를 수행합니다.
   > ./gradlew build


3. 내장 Tomcat을 기동합니다.
   > Port : `8008`<br/>
   > Context Path : `/AprFriend`<br/>
   > Base URL : [http://localhost:8008/AprFriend](http://localhost:8008/AprFriend)<br/>
<br/>

2️⃣ 테스트 도구 선택(테스트는 Swagger UI 또는 Postman으로 진행할 수 있습니다.)

<details> <summary><b>🧭 Swagger UI</b></summary>
URL: http://localhost:8008/AprFriend/swagger-ui/index.html

Swagger에 헤더, 바디 등 필요한 설정이 사전 등록되어 있습니다.
→ 별도의 설정 없이 바로 실행 가능합니다.

</details> <details> <summary><b>📮 Postman</b></summary>
  역할&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;user-Id<br/>
  친구 신청을 하는 사용자&emsp;&emsp;20<br/>
  친구 신청을 받는 사용자&emsp;&emsp;100044737<br/><br/>
테스트는 아래 순서대로 진행하세요.<br/>
(모든 요청의 기본 URL은 http://127.0.0.1:8008/AprFriend 입니다.)<br/><br/>

</details><br/>
3️⃣ API 테스트 시나리오
<details open> <summary><b>1. 친구 목록 조회</b></summary>
**GET** http://127.0.0.1:8008/AprFriend/api/friends?page=0&maxSize=20&sort=approvedAt,desc


- Header

  X-user-Id : 100044737


✅ 설명: 승인된 친구 목록을 승인일 기준 내림차순으로 조회합니다.<br/>

</details>
<details> <summary><b>2. 받은 친구 신청 목록 조회</b></summary>
**GET** http://127.0.0.1:8008/AprFriend/api/friends/requests?maxSize=20&window=1d&sort=requestedAt,desc


- Header

  X-user-Id : 100044737


✅ 설명: 최근 하루(window=1d) 내 받은 친구 신청 목록을 조회합니다.<br/>

</details>
<details> <summary><b>3. 친구 신청 보내기</b></summary>
**POST** http://127.0.0.1:8008/AprFriend/api/friends/request


- Header

  X-user-Id : 20


- Body (JSON)

  {
    "toUserId": "100044737"
  }


✅ 설명: userId=20 사용자가 userId=100044737에게 친구 신청을 요청합니다.<br/>

</details>
<details> <summary><b>4. 친구 신청 승인</b></summary>
**POST** http://127.0.0.1:8008/AprFriend/api/friends/accept/{requestId}


- Header

  X-user-Id: 100044737


- Path Variable

  {requestId} = 2번에서 조회한 requestId 값


✅ 설명: 요청받은 친구 신청을 승인합니다.<br/>

</details>
<details> <summary><b>5. 친구 신청 거절</b></summary>
**POST** http://127.0.0.1:8008/AprFriend/api/friends/reject/{requestId}


- Header

  X-user-Id: 100044737


- Path Variable

  {requestId} = 2번에서 조회한 requestId 값


✅ 설명: 요청받은 친구 신청을 거절합니다.<br/>

</details>

끝까지 읽어주셔서 감사합니다 🙏
저의 코드와 구조를 통해 문제를 어떻게 풀어가는지 봐주시면 감사하겠습니다.
