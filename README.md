## 🧑‍🤝‍🧑 apr-backend-assignment
안녕하세요. APR 백엔드 직무에 지원한 **이민영**입니다.<br/>
읽어주심에 감사드리며, 향후 좋은 인연이 되면 좋겠습니다.<br/>
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
GET http://127.0.0.1:8008/AprFriend/api/friends?page=0&maxSize=20&sort=approvedAt,desc


- Header

  X-user-Id : 100044737


> 승인된(`APPROVED`) 친구 목록을 승인일 기준 내림차순으로 조회합니다.<br/>
> `Pageable` 기반으로 페이징 처리를 적용하여 대규모 데이터 조회 시 효율을 확보했습니다.<br/>

</details>
<details> <summary><b>2. 받은 친구 신청 목록 조회</b></summary>
GET http://127.0.0.1:8008/AprFriend/api/friends/requests?maxSize=20&window=1d&sort=requestedAt,desc


- Header

  X-user-Id : 100044737


> 최근 일정 기간(`window` 파라미터: 1d, 7d, 30d, 90d, over)에 받은 요청을 조회합니다.<br/>
> `window` 값에 따라 조회 기간을 동적으로 계산하였습니다.(`OffsetDateTime.minusDays` 활용)<br/>

</details>
<details> <summary><b>3. 친구 신청 보내기</b></summary>
POST http://127.0.0.1:8008/AprFriend/api/friends/request


- Header

  X-user-Id : 20


- Body (JSON)

  {
    "toUserId": "100044737"
  }


> 자기 자신에게는 요청할 수 없습니다.<br/>
> 이미 **대기 중(PENDING)** 상태의 요청이 존재하면 **중복 신청 방지** 예외처리를 하였습니다.<br/>
> 요청 정보는 `OffsetDateTime.now(ZoneOffset.UTC)` 기준으로 기록하여 서버 지역과 관계없이 **UTC 시간 일관성**을 유지합니다.<br/>

</details>
<details> <summary><b>4. 친구 신청 승인</b></summary>
POST http://127.0.0.1:8008/AprFriend/api/friends/accept/{requestId}


- Header

  X-user-Id: 100044737


- Path Variable

  {requestId} = 2번에서 조회한 requestId 값


> 승인 권한은 요청을 받은 사용자(`toUserId`)만 가집니다.<br/>
> 친구 수가 10,000명을 초과하면 `400 BAD_REQUEST` 반환합니다.<br/>
> 승인 시 `APPROVED` 상태로 변경 후 `approvedAt`에 승인 일시를 기록합니다.<br/>

</details>
<details> <summary><b>5. 친구 신청 거절</b></summary>
POST http://127.0.0.1:8008/AprFriend/api/friends/reject/{requestId}


- Header

  X-user-Id: 100044737


- Path Variable

  {requestId} = 2번에서 조회한 requestId 값


> 승인과 동일하게, 요청받은 사용자만 거절할 수 있습니다.<br/>
> 거절 시 해당 요청 레코드를 삭제합니다.<br/>

</details>
<br/>

## 🙏 마무리하며
끝까지 읽어주셔서 감사합니다.<br/>
과제를 통해 저의 개발 역량과 문제 해결 과정을 진정성 있게 보여드리고자 하였습니다.<br/>
좋은 기회 주셔서 감사드리며, 성실하게 임하겠습니다.
