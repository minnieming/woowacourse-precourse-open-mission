# 코딩테스트 알고리즘 추천 서비스

## 1. 프로젝트 설명

이 프로젝트는 코딩테스트 문제를 입력하면, 문제 성격과 입력 크기(시간 복잡도)를 기반으로 적합한 알고리즘을 추천하고 이유를 설명해주는 API 서버다.
Ktor 기반의 Kotlin 백엔드로 구성되어 있으며, 일부 단계에서 OpenAI를 활용해 알고리즘 태그 예측 및 자연어 설명을 생성한다.



## 2. 프로젝트 기능

   •	문제 분석 API 제공

   •	문제 텍스트를 받아 알고리즘 후보를 생성

   •	AI 기반 알고리즘 태그 예측

   •	GPT를 이용해 문제에 맞는 알고리즘 키워드/태그 추천

   •	시간 복잡도 기반 필터링

   •	입력 크기에 따라 허용 가능한 시간 복잡도를 계산하고 부적합한 알고리즘 제외

   •	추천 결과 + 설명 반환

   •	최종 추천 알고리즘 리스트와 추천 이유/요약을 JSON으로 반환

   •	테스트 코드 포함

   •	주요 서비스 로직 단위 테스트 지원


## 3. 프로젝트 구조 & 주요 클래스 역할

```aiignore
.
├── main
│   ├── kotlin
│   │   ├── Application.kt
│   │   ├── controller
│   │   │   └── ProblemAnalyzeController.kt
│   │   ├── domain
│   │   │   ├── AlgorithmCatalog.kt
│   │   │   └── AlgorithmModels.kt
│   │   ├── HTTP.kt
│   │   ├── Routing.kt
│   │   └── service
│   │       ├── AiAlgorithmPredictService.kt
│   │       ├── AiExplanationService.kt
│   │       └── ProblemAnalyzeService.kt
│   └── resources
│       ├── application.yaml
│       ├── logback.xml
│       └── openapi
│           └── documentation.yaml
└── test
    └── kotlin
        ├── ApplicationTest.kt
        └── service
            ├── AiAlgorithmPredictServiceTest.kt
            ├── AiExplanationServiceTest.kt
            └── ProblemAnalyzeServiceTest.kt
```

###  main/kotlin
•	Application.kt

Ktor 서버 진입점. 플러그인(ContentNegotiation 등) 설치 및 Controller 연결.

•	controller/ProblemAnalyzeController.kt

/analyze 같은 API 엔드포인트 정의 및 요청을 받아 서비스 호출 → 응답 반환 담당.

•	domain/AlgorithmCatalog.kt

프로젝트가 가진 알고리즘 목록/기본 정보(카탈로그) 정의.

•	domain/AlgorithmModels.kt

AlgorithmInfo, AlgorithmResult, AnalyzeResponse 같은 DTO/모델 클래스 모음.

•	HTTP.kt

HTTP 관련 공통 설정(상태코드, 응답 포맷 등) 담당.

•	Routing.kt

라우팅(경로) 등록을 분리해둔 파일. Controller 라우트 연결.

•	service/AiAlgorithmPredictService.kt

OpenAI 호출로 문제에 맞는 알고리즘 태그 예측.

•	service/AiExplanationService.kt

OpenAI 호출로 추천 이유/설명 문장 생성.

•	service/ProblemAnalyzeService.kt

프로젝트 핵심 로직.
입력 크기 기반 시간복잡도 추정 → AI 예측 결과와 합쳐 추천/제외 결정.

### main/resources
•	application.yaml

서버 설정 파일(포트, 환경 변수 등).

•	logback.xml

로깅 레벨/포맷 설정.

•	openapi/documentation.yaml

OpenAPI(Swagger) 문서 정의.

### test/kotlin
•	ApplicationTest.kt

서버/라우팅 기본 동작 테스트.

•	AiAlgorithmPredictServiceTest.kt

알고리즘 태그 예측 서비스 테스트.

•	AiExplanationServiceTest.kt

설명 생성 서비스 테스트.

•	ProblemAnalyzeServiceTest.kt

시간복잡도 필터링 + 추천 로직 핵심 테스트.