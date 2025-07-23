# 🧪 SpecTestEngine

**SpecTestEngine** is a lightweight REST API test runner that executes HTTP tests based on simple JSON specifications.

---

## 🚀 Features

- ✅ Declarative API testing via JSON specifications
- ✅ Status code, media type, and body checks
- ✅ Supports `excludedFields` to ignore dynamic fields
- ✅ Supports `excludeAllOtherBodyFields` to validate only specified parts of the body
- ✅ Supports `expectedBodyPath & expectedBodyValue` **JsonPath** checks to verify specific nested JSON values
- ✅ Detailed run logs for every test execution
- ✅ Each specification and its test results are saved to the database with full CRUD support
- ✅ Built with **Spring Boot** and **RestAssured**

---

## 📑 API Test Specification

| Field                      | Description                                                                                              |
|----------------------------|----------------------------------------------------------------------------------------------------------|
| **url**                    | The API endpoint URL to test                                                                             |
| **method**                 | HTTP method (`GET`, `POST`, `PUT`, `DELETE`, etc.)                                                       |
| **expectedStatus**         | Expected HTTP response status code                                                                       |
| **expectedMediaType**      | *(Optional)* Expected `Content-Type` header                                                              |
| **expectedBody**           | *(Optional)* JSON body to compare                                                                        |
| **excludedFields**         | *(Optional)* Array of JSON fields to ignore during the body comparison                                   |
| **excludeAllOtherBodyFields** | *(Optional)* If `true`, trims the actual response body to include only the specified expected fields      |
| **expectedBodyPath**       | *(Optional)* JSONPath expression to locate a value in the response body                                  |
| **expectedBodyValue**      | *(Optional)* The expected value at the given JSONPath                                                    |

---

## ⚙️ How it works

1. Parses the JSON test specification.
2. Executes the HTTP request using RestAssured.
3. Runs the following checks:
   - ✅ **Status code** check
   - ✅ **Media type** check (if provided)
   - ✅ **Body** check:
     - Ignores `excludedFields` if specified.
     - If `excludeAllOtherBodyFields` is `true`, trims the actual body to include only the expected fields.
     - If `expectedBodyPath` and `expectedBodyValue` are specified, performs a JSONPath check.
4. Logs all intermediate steps and stores a detailed `run log` for every test.
5. Saves the test run with a final `PASS` or `FAIL` status.
6. All test specs and results are persisted in the database and support full CRUD operations via the REST API.

---

## 📌 `excludeAllOtherBodyFields`

When `excludeAllOtherBodyFields` is `true`, the handler removes all fields in the actual response except those explicitly defined in `expectedBody`.
This helps ignore irrelevant or dynamic data (like timestamps, auto-generated IDs, or unrelated nested objects).

✅ If the trimmed actual body exactly matches the `expectedBody`, the check passes.

---

## 📌 `JsonPath` Check

When both `expectedBodyPath` and `expectedBodyValue` are provided:

- The runner extracts the value from the actual response using the JSONPath expression.
- If the extracted value matches `expectedBodyValue`, the check passes.

This is useful for verifying deeply nested properties or array elements without needing to compare the whole response body.

---
---
## ✅ Example of success test specification
```json
{
    "url": "http://localhost:8080/test/spec",
    "method": "GET",
    "expectedStatus": 200,
    "expectedMediaType": "application/json",
    "expectedBody": {
        "name": "success test",
        "spec": {
            "url": "http://localhost:8080/test/spec",
            "method": "GET",
            "expectedStatus": 200
        }
    },
    "excludeAllOtherBodyFields": true
}
```

## ✅ Example of success test run log

```json
{
    "runId": 1,
    "specId": 1,
    "status": "----------------PASS-------------------",
    "log": {
        "url": "http://localhost:8080/test/spec",
        "method": "GET",
        "expectedStatus": 200,
        "actualStatus": 200,
        "statusCodeCheckResult": "----------------PASS-------------------",
        "expectedMediaType": "application/json",
        "actualMediaType": "application/json",
        "mediaTypeCheckResult": "----------------PASS-------------------",
        "expectedBody": {
            "name": "success test",
            "spec": {
                "url": "http://localhost:8080/test/spec",
                "method": "GET",
                "expectedStatus": 200
            }
        },
        "actualBody": [
            {
                "name": "success test",
                "spec": {
                    "url": "http://localhost:8080/test/spec",
                    "method": "GET",
                    "expectedStatus": 200
                }
            }
        ],
        "bodyCheckResult": "----------------PASS-------------------"
    },
    "startedAt": "2025-07-23T17:39:49",
    "finishedAt": "2025-07-23T17:39:50"
}
```

---
## ❌ Example of fail test specification
```json
{
    "url": "http://localhost:8080/test/spec",
    "method": "GET",
    "expectedStatus": 404,
    "expectedMediaType": "application/text",
    "expectedBody": {
        "name": "success test",
        "spec": {
            "url": "http://localhost:8080/test/spec",
            "method": "GET",
            "expectedStatus": 200
        }
    },
    "excludeAllOtherBodyFields": true
}
```

## ❌ Example of fail test run log

```json
{
    "runId": 1,
    "specId": 1,
    "status": "----------------FAIL-------------------",
    "log": {
        "url": "http://localhost:8080/test/spec",
        "method": "GET",
        "expectedStatus": 404,
        "actualStatus": 200,
        "statusCodeCheckResult": "----------------FAIL-------------------",
        "expectedMediaType": "application/text",
        "actualMediaType": "application/json",
        "mediaTypeCheckResult": "----------------FAIL-------------------",
        "expectedBody": {
            "name": "success test",
            "spec": {
                "url": "http://localhost:8080/test/spec",
                "method": "GET",
                "expectedStatus": 200
            }
        },
        "actualBody": [
            {
                "name": "fail test",
                "spec": {
                    "url": "http://localhost:8080/test/spec",
                    "method": "GET",
                    "expectedStatus": 404
                }
            }
        ],
        "bodyCheckResult": "----------------FAIL-------------------"
    },
    "startedAt": "2025-07-23T17:49:54",
    "finishedAt": "2025-07-23T17:49:55"
}
```
---
## 🎯 TODO List for Future Improvements

- 📌 Add swagger documentation for active examples.
- 📌 Add more handlers.

---
## 🧑‍💻 Contributing
Feel free to fork, extend and open PRs!

Ideas: more matchers, authentication flows, test suites, CI/CD integration, UI dashboards.