# 🧪 SpecTestEngine

**SpecTestEngine** is a lightweight REST API test runner that executes HTTP tests based on simple JSON specifications.

---

## 🚀 Features

- ✅ Declarative API testing with JSON specs
- ✅ Status code, media type, and body validation
- ✅ Supports `excludedBodyFields` to ignore dynamic fields
- ✅ Supports `excludeAllOtherBodyFields` to compare only specified fields
- ✅ Supports **multiple JSONPath checks** (`expectedBodyJsonPaths`)
- ✅ Flexible **headers** and **body** definition in the spec
- ✅ Asynchronous **queue** system:
    - Parallel execution for different URLs
    - Sequential execution per single URL (FIFO)
- ✅ Detailed run logs for every test execution
- ✅ Full CRUD for saving specifications and results
- ✅ Powered by **Spring Boot**, **RestAssured**, and **Java 21 Virtual Threads**

---

## 📑 API Test Specification

| Field                      | Description                                                                                              |
|----------------------------|----------------------------------------------------------------------------------------------------------|
| **url**                    | The API endpoint URL to test                                                                             |
| **method**                 | HTTP method (`GET`, `POST`, `PUT`, `DELETE`, etc.)                                                       |
| **headers**                | *(Optional)* Key-value map for HTTP headers                                                              |
| **body**                   | *(Optional)* JSON body for `POST` or `PUT` requests                                                      |
| **expectedStatusCode**     | Expected HTTP response status code                                                                       |
| **expectedMediaType**      | *(Optional)* Expected `Content-Type` header                                                              |
| **expectedBody**           | *(Optional)* JSON body to compare                                                                        |
| **excludedBodyFields**     | *(Optional)* Array of JSON fields to ignore during body comparison                                       |
| **excludeAllOtherBodyFields** | *(Optional)* If `true`, trims the actual response body to only keep fields present in `expectedBody`     |
| **expectedBodyJsonPaths**  | *(Optional)* Array of JSONPath checks: `{ "expectedJsonPath": "foo", "expectedJsonValue": "bar" }`     |

---

## ⚙️ How it works

1. Parses the JSON specification.
2. Builds an HTTP request with **headers** and **body** if provided.
3. Executes the request using **RestAssured**.
4. Runs the following checks:
    - ✅ **Status code**: matches `expectedStatusCode`
    - ✅ **Media type**: matches `expectedMediaType` (if provided)
    - ✅ **Body check**:
        - Ignores `excludedBodyFields` if specified.
        - If `excludeAllOtherBodyFields` is `true`, trims the actual body to only the fields in `expectedBody`.
    - ✅ **Multiple JSONPath checks**: validates each `expectedBodyJsonPaths` item.
5. Stores a detailed log with all intermediate steps.
6. Saves the final result with `PASS` or `FAIL` status.
7. All tests run through an **internal queue** to:
    - Execute requests **in parallel** for different URLs.
    - Execute requests **sequentially** for the same URL.

---

## 📌 `excludeAllOtherBodyFields`

When `excludeAllOtherBodyFields` is `true`, the handler removes all fields in the actual response body except those defined in `expectedBody`.  
This is useful for ignoring dynamic or irrelevant data.

✅ If the `trimmed received body` exactly matches the `expectedBody`, the check passes.

---

## 📌 `JsonPath` Check

- The `expectedBodyJsonPaths` field accepts an **array of JSONPath checks**:
  ```json
  "expectedBodyJsonPaths": [
    { "expectedJsonPath": "data.id", "expectedJsonValue": "123" },
    { "expectedJsonPath": "data.name", "expectedJsonValue": "Alex" }
  ]
---
## ✅ Example of success test specification
```json
{
  "url": "http://localhost:8080/test/spec/create?specName=POST-TEST",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer abc123",
    "X-Custom-Header": "test"
  },
  "body": {
    "url": "http://localhost:8080/test/spec/create?specName=POST-TEST",
    "method": "POST",
    "name": "Alex",
    "email": "alexandr.bezarov@gmail.com"
  },
  "expectedStatusCode": 200,
  "expectedMediaType": "application/json",
  "expectedBody": {
    "id": 1,
    "name": "POST-TEST",
    "spec": {
      "name": "Alex",
      "email": "alexandr.bezarov@gmail.com"
    },
    "createdAt": "2025-07-27T14:28:18"
  },
  "excludedBodyFields": [
    "id",
    "createdAt"
  ],
  "excludeAllOtherBodyFields": false,
  "expectedBodyJsonPaths": [
    {
      "expectedJsonPath": "spec.name",
      "expectedJsonValue": "Alex"
    },
    {
      "expectedJsonPath": "spec.email",
      "expectedJsonValue": "alexandr.bezarov@gmail.com"
    }
  ]
}
```

## ✅ Example of success test run log

```json
{
  "runId": 1,
  "specId": 1,
  "overallTestStatus": "----------------PASS-------------------",
  "log": {
    "url": "http://localhost:8080/test/spec/create?specName=POST-TEST",
    "method": "POST",
    "expectedStatusCode": 200,
    "receivedStatusCode": 200,
    "statusCodeCheckResult": "----------------PASS-------------------",
    "expectedMediaType": "application/json",
    "receivedMediaType": "application/json",
    "mediaTypeCheckResult": "----------------PASS-------------------",
    "expectedJsonPathCheck": [
      {
        "expectedJsonPath": "spec.name",
        "expectedJsonValue": "Alex",
        "receivedJsonValue": "Alex",
        "bodyJsonPathValueCheckResult": "----------------PASS-------------------"
      },
      {
        "expectedJsonPath": "spec.email",
        "expectedJsonValue": "alexandr.bezarov@gmail.com",
        "receivedJsonValue": "alexandr.bezarov@gmail.com",
        "bodyJsonPathValueCheckResult": "----------------PASS-------------------"
      }
    ],
    "expectedBody": {
      "name": "POST-TEST",
      "spec": {
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      }
    },
    "comparedBody": {
      "name": "POST-TEST",
      "spec": {
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      }
    },
    "bodyCheckResult": "----------------PASS-------------------",
    "receivedBody": {
      "id": 2,
      "name": "POST-TEST",
      "spec": {
        "url": "http://localhost:8080/test/spec/create?specName=POST-TEST",
        "method": "POST",
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      },
      "createdAt": "2025-07-27T14:38:40"
    }
  },
  "startedAt": "2025-07-27T14:38:40",
  "finishedAt": "2025-07-27T14:38:41"
}
```

---
## ❌ Example of fail test specification
```json
{
  "url": "http://localhost:8080/test/spec/create?specName=FAIL-POST-TEST",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer abc123",
    "X-Custom-Header": "test"
  },
  "body": {
    "url": "http://localhost:8080/test/spec/create?specName=FAIL-POST-TEST",
    "method": "POST",
    "name": "Alex",
    "email": "alexandr.bezarov@gmail.com"
  },
  "expectedStatusCode": 400,
  "expectedMediaType": "application/text",
  "expectedBody": {
    "id": 1,
    "name": "FAIL-POST-TEST",
    "spec": {
      "name": "Alex",
      "email": "alexandr.bezarov@gmail.com"
    },
    "createdAt": "2025-07-27T14:28:18"
  },
  "excludedBodyFields": [
    "createdAt"
  ],
  "excludeAllOtherBodyFields": false,
  "expectedBodyJsonPaths": [
    {
      "expectedJsonPath": "spec.name",
      "expectedJsonValue": "Alex1"
    },
    {
      "expectedJsonPath": "spec.email",
      "expectedJsonValue": "alexandr.bezarov@gmail.com"
    }
  ]
}
```

## ❌ Example of fail test run log

```json
{
  "runId": 1,
  "specId": 1,
  "overallTestStatus": "----------------FAIL-------------------",
  "log": {
    "url": "http://localhost:8080/test/spec/create?specName=FAIL-POST-TEST",
    "method": "POST",
    "expectedStatusCode": 400,
    "receivedStatusCode": 200,
    "statusCodeCheckResult": "----------------FAIL-------------------",
    "expectedMediaType": "application/text",
    "receivedMediaType": "application/json",
    "mediaTypeCheckResult": "----------------FAIL-------------------",
    "expectedJsonPathCheck": [
      {
        "expectedJsonPath": "spec.name",
        "expectedJsonValue": "Alex1",
        "receivedJsonValue": "Alex",
        "bodyJsonPathValueCheckResult": "----------------FAIL-------------------"
      },
      {
        "expectedJsonPath": "spec.email",
        "expectedJsonValue": "alexandr.bezarov@gmail.com",
        "receivedJsonValue": "alexandr.bezarov@gmail.com",
        "bodyJsonPathValueCheckResult": "----------------PASS-------------------"
      }
    ],
    "expectedBody": {
      "id": 1,
      "name": "FAIL-POST-TEST",
      "spec": {
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      }
    },
    "comparedBody": {
      "id": 2,
      "name": "FAIL-POST-TEST",
      "spec": {
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      }
    },
    "bodyCheckResult": "----------------FAIL-------------------",
    "receivedBody": {
      "id": 2,
      "name": "FAIL-POST-TEST",
      "spec": {
        "url": "http://localhost:8080/test/spec/create?specName=FAIL-POST-TEST",
        "method": "POST",
        "name": "Alex",
        "email": "alexandr.bezarov@gmail.com"
      },
      "createdAt": "2025-07-31T12:09:03"
    }
  },
  "startedAt": "2025-07-31T12:09:03",
  "finishedAt": "2025-07-31T12:09:04"
}
```
---

## ⚡ Execution Queue
- All incoming test runs are added to a queue based on the request URL.
- Tasks for the same URL run one by one in the order they arrive.
- Tasks for different URLs run in parallel using Virtual Threads.

---
## 🎯 TODO List for Future Improvements

- 🔒 Add authentication and role-based access control
- 📚 Provide Swagger/OpenAPI documentation with live examples
- 🖥️ Build a simple UI dashboard to monitor queues and results
- 🧩 Add more matchers and check handlers (XML, schema validation, etc.)

---
## 🧑‍💻 Contributing
- Feel free to fork, extend and open PRs!
- Ideas: more matchers, authentication flows, CI/CD integration, dashboards, cloud-native execution.