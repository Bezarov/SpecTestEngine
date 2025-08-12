# 🧪 SpecTestEngine

**SpecTestEngine** is a lightweight REST API test runner that executes HTTP tests based on simple **JSON, YAML or XML** specifications.

---

## 🚀 Features

- ✅ Declarative API testing with **JSON, YAML or XML** specifications
- ✅ Status code, Content-type, and Body validation
- ✅ Supports `excludedBodyFields` to ignore dynamic fields
- ✅ Supports `excludeAllOtherBodyFields` to compare only specified fields
- ✅ Supports **multiple BodyPath checks** (`expectedBodyPaths`)
- ✅ Flexible **headers** and **body** definition in the specification
- ✅ Asynchronous **queue** system:
    - Parallel execution for different URLs
    - Sequential execution per single URL (FIFO)
- ✅ Detailed run logs for every test execution
- ✅ Full CRUD for saving specifications and results
- ✅ Supports **JSON, YAML or XML** formats for specs, tests and test results

- ✅ Powered by **Spring Boot**, **RestAssured**, and **Java 21 Virtual Threads**

---

## 📑 API Test Specification
> ℹ️ Test specification can be written in either **JSON, YAML or XML** format. 

> The request will be executed in the same format as the specification.
 
> The response will be returned in the same format as the specification.

| Field                         | Description                                                                                          |
|-------------------------------|------------------------------------------------------------------------------------------------------|
| **url**                       | The API endpoint URL to test                                                                         |
| **method**                    | HTTP method (`GET`, `POST`, `PUT`, `DELETE`, etc.)                                                   |
| **headers**                   | *(Optional)* Key-value map for HTTP headers                                                          |
| **body**                      | *(Optional)* JSON body for `POST` or `PUT` requests                                                  |
| **expectedStatusCode**        | Expected HTTP response status code                                                                   |
| **expectedContentType**         | *(Optional)* Expected `Content-Type` header                                                          |
| **expectedBody**              | *(Optional)* JSON body to compare                                                                    |
| **excludedBodyFields**        | *(Optional)* Array of JSON fields to ignore during body comparison                                   |
| **excludeAllOtherBodyFields** | *(Optional)* If `true`, trims the actual response body to only keep fields present in `expectedBody` |
| **expectedBodyPaths**         | *(Optional)* Array of Body-path checks: `{ "expectedBodyPath": "foo", "expectedBodyValue": "bar" }`  |

---

## ⚙️ How it works

1. Parses the specification (JSON, YAML or XML).
2. Builds an HTTP request with **headers** and **body in the same format as the specification**  if provided.
3. Executes the request using **RestAssured**.
4. Runs the following checks:
    - ✅ **Status code**: matches `expectedStatusCode`
    - ✅ **Content type**: matches `expectedContentType` (if provided)
    - ✅ **Body check**:
        - Ignores `excludedBodyFields` if specified.
        - If `excludeAllOtherBodyFields` is `true`, trims the received body to only the fields in `expectedBody`.
    - ✅ **Multiple JSONPath checks**: validates each `expectedBodyPaths` item.
5. Stores a detailed result log with all intermediate steps.
6. Saves the final result with `PASS` or `FAIL` status.
7. All tests run through an **internal queue** to:
    - Execute requests **in parallel** for different URLs.
    - Execute requests **sequentially** for the same URL.

---

## 📌 `excludeAllOtherBodyFields`

When `excludeAllOtherBodyFields` is `true`, the handler removes all fields in the received response body except those defined in `expectedBody`.  
This is useful for ignoring dynamic or irrelevant data.

✅ If the `trimmed received body` exactly matches the `expectedBody`, the check passes.

---

## 📌 `BodyPathCheck`

- The `expectedBodyPaths` field accepts an **array of BodyPath checks**:
  ```json
  "expectedBodyPaths": [
    { "expectedBodyPath": "data.id", "expectedBodyValue": 123 },
    { "expectedBodyPath": "data.name", "expectedBodyValue": "Alex" },
    { "expectedBodyPath": "data.active", "expectedBodyValue": true },
    { "expectedBodyPath": "data.courses", "expectedBodyValue": ["IT", "Network", "Development"] },
  ]

---

## ⚡ Execution Queue
- All incoming test runs are added to a queue based on the request URL.
- Tasks for the same URL run one by one in the order they arrive.
- Tasks for different URLs run in parallel using Virtual Threads.

---
## ✅ Example of YAML formatted success test specification
```yaml
url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST"
method: "POST"
headers:
  Authorization: "Bearer abc123"
  X-Custom-Header: "test"
body:
  url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST"
  method: "POST"
  name: "Alex"
  age: 33
  active: true
  emails:
    - "alexandr.bezarov@gmail.com"
    - "bezarov.sasha@gmail.com"
expectedStatusCode: 200
expectedContentType: "application/yaml"
expectedBody:
  id: 1
  name: "YAML-POST-TEST"
  spec:
    name: "Alex"
    age: 33
    active: true
    emails:
      - "alexandr.bezarov@gmail.com"
      - "bezarov.sasha@gmail.com"
  createdAt: "2025-07-27T14:28:18"
excludedBodyFields:
  - "id"
  - "createdAt"
excludeAllOtherBodyFields: false
expectedBodyPaths:
  - expectedBodyPath: "spec.name"
    expectedBodyValue: "Alex"
  - expectedBodyPath: "spec.emails"
    expectedBodyValue:
      - "alexandr.bezarov@gmail.com"
      - "bezarov.sasha@gmail.com"
  - expectedBodyPath: "spec.age"
    expectedBodyValue: 33
  - expectedBodyPath: "spec.active"
    expectedBodyValue: true
```
## ✅ Example of YAML formatted success test run log

```yaml
runId: 2
specId: 3
overallTestStatus: "----------------PASS-------------------"
testResultLog:
  url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST"
  method: "POST"
  expectedStatusCode: 200
  receivedStatusCode: 200
  statusCodeCheckResult: "----------------PASS-------------------"
  expectedContentType: "application/yaml"
  receivedContentType: "application/yaml"
  contentTypeCheckResult: "----------------PASS-------------------"
  expectedBodyPathCheck:
    - expectedBodyPath: "spec.name"
      expectedBodyValue: "Alex"
      receivedBodyValue: "Alex"
      bodyPathValueCheckResult: "----------------PASS-------------------"
    - expectedBodyPath: "spec.emails"
      expectedBodyValue:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
      receivedBodyValue:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
      bodyPathValueCheckResult: "----------------PASS-------------------"
    - expectedBodyPath: "spec.age"
      expectedBodyValue: 33
      receivedBodyValue: 33
      bodyPathValueCheckResult: "----------------PASS-------------------"
    - expectedBodyPath: "spec.active"
      expectedBodyValue: true
      receivedBodyValue: true
      bodyPathValueCheckResult: "----------------PASS-------------------"
  expectedBody:
    name: "YAML-POST-TEST"
    spec:
      name: "Alex"
      age: 33
      active: true
      emails:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
  comparedBody:
    name: "YAML-POST-TEST"
    spec:
      name: "Alex"
      age: 33
      active: true
      emails:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
  bodyCheckResult: "----------------PASS-------------------"
  receivedBody:
    id: 4
    name: "YAML-POST-TEST"
    spec:
      url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST"
      method: "POST"
      name: "Alex"
      age: 33
      active: true
      emails:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
    createdAt: "2025-08-12T21:24:41"
startedAt: "2025-08-12T21:24:41"
finishedAt: "2025-08-12T21:24:41"
```
---
## ❌ Example of YAML formatted fail test specification
```yaml
url: 'http://localhost:8080/test/spec/create?specName=YAML-POST-TEST'
method: POST
headers:
  Authorization: Bearer abc123
  X-Custom-Header: test
body:
  url: 'http://localhost:8080/test/spec/create?specName=YAML-POST-TEST-FOR-FAIL'
  method: POST
  name: Alex1
  age: '33'
  active: 'true'
  emails:
    - alexandr.bezarov@gmail.com@gmail.com
    - bezarov.sasha1@gmail.com
expectedStatusCode: 404
expectedContentType: application/text
expectedBody:
  id: 1
  name: JSON-POST-TEST
  spec:
    name: Alex
    age: 33
    active: true
    emails:
      - alexandr.bezarov@gmail.com
      - bezarov.sasha@gmail.com
  createdAt: '2025-07-27T14:28:18'
excludedBodyFields:
  - id
  - createdAt
excludeAllOtherBodyFields: false
expectedBodyPaths:
  - expectedBodyPath: spec.name
    expectedBodyValue: Alex
  - expectedBodyPath: spec.emails
    expectedBodyValue:
      - alexandr.bezarov@gmail.com
      - bezarov.sasha@gmail.com
  - expectedBodyPath: spec.age
    expectedBodyValue: 33
  - expectedBodyPath: spec.active
    expectedBodyValue: true
```
## ❌ Example of YAML formatted fail test run log

```yaml
---
runId: 1
specId: 1
overallTestStatus: "----------------FAIL-------------------"
testResultLog:
  url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST"
  method: "POST"
  expectedStatusCode: 404
  receivedStatusCode: 200
  statusCodeCheckResult: "----------------FAIL-------------------"
  expectedContentType: "application/text"
  receivedContentType: "application/yaml"
  contentTypeCheckResult: "----------------FAIL-------------------"
  expectedBodyPathCheck:
    - expectedBodyPath: "spec.name"
      expectedBodyValue: "Alex"
      receivedBodyValue: "Alex1"
      bodyPathValueCheckResult: "----------------FAIL-------------------"
    - expectedBodyPath: "spec.emails"
      expectedBodyValue:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
      receivedBodyValue:
        - "alexandr.bezarov@gmail.com@gmail.com"
        - "bezarov.sasha1@gmail.com"
      bodyPathValueCheckResult: "----------------FAIL-------------------"
    - expectedBodyPath: "spec.age"
      expectedBodyValue: 33
      receivedBodyValue: "33"
      bodyPathValueCheckResult: "----------------FAIL-------------------"
    - expectedBodyPath: "spec.active"
      expectedBodyValue: true
      receivedBodyValue: "true"
      bodyPathValueCheckResult: "----------------FAIL-------------------"
  expectedBody:
    name: "JSON-POST-TEST"
    spec:
      name: "Alex"
      age: 33
      active: true
      emails:
        - "alexandr.bezarov@gmail.com"
        - "bezarov.sasha@gmail.com"
  comparedBody:
    name: "YAML-POST-TEST"
    spec:
      name: "Alex1"
      age: "33"
      active: "true"
      emails:
        - "alexandr.bezarov@gmail.com@gmail.com"
        - "bezarov.sasha1@gmail.com"
  bodyCheckResult: "----------------FAIL-------------------"
  receivedBody:
    id: 2
    name: "YAML-POST-TEST"
    spec:
      url: "http://localhost:8080/test/spec/create?specName=YAML-POST-TEST-FOR-FAIL"
      method: "POST"
      name: "Alex1"
      age: "33"
      active: "true"
      emails:
        - "alexandr.bezarov@gmail.com@gmail.com"
        - "bezarov.sasha1@gmail.com"
    createdAt: "2025-08-12T21:46:18"
startedAt: "2025-08-12T21:46:17"
finishedAt: "2025-08-12T21:46:18"

```

---

## ✅ Example of JSON formatted success test specification
```json
{
  "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer abc123",
    "X-Custom-Header": "test"
  },
  "body": {
    "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
    "method": "POST",
    "name": "Alex",
    "age": 33,
    "active": true,
    "emails": [
      "alexandr.bezarov@gmail.com",
      "bezarov.sasha@gmail.com"
    ]
  },
  "expectedStatusCode": 200,
  "expectedContentType": "application/json",
  "expectedBody": {
    "id": 1,
    "name": "JSON-POST-TEST",
    "spec": {
      "name": "Alex",
      "age": 33,
      "active": true,
      "emails": [
        "alexandr.bezarov@gmail.com",
        "bezarov.sasha@gmail.com"
      ]
    },
    "createdAt": "2025-07-27T14:28:18"
  },
  "excludedBodyFields": [
    "id",
    "createdAt"
  ],
  "excludeAllOtherBodyFields": false,
  "expectedBodyPaths": [
    {
      "expectedBodyPath": "spec.name",
      "expectedBodyValue": "Alex"
    },
    {
      "expectedBodyPath": "spec.emails",
      "expectedBodyValue": [
        "alexandr.bezarov@gmail.com",
        "bezarov.sasha@gmail.com"
      ]
    },
    {
      "expectedBodyPath": "spec.age",
      "expectedBodyValue": 33
    },
    {
      "expectedBodyPath": "spec.active",
      "expectedBodyValue": true
    }
  ]
}
```

## ✅ Example of JSON formatted success test run log

```json
{
  "runId": 1,
  "specId": 1,
  "overallTestStatus": "----------------PASS-------------------",
  "testResultLog": {
    "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
    "method": "POST",
    "expectedStatusCode": 200,
    "receivedStatusCode": 200,
    "statusCodeCheckResult": "----------------PASS-------------------",
    "expectedContentType": "application/json",
    "receivedContentType": "application/json",
    "contentTypeCheckResult": "----------------PASS-------------------",
    "expectedBodyPathCheck": [
      {
        "expectedBodyPath": "spec.name",
        "expectedBodyValue": "Alex",
        "receivedBodyValue": "Alex",
        "bodyPathValueCheckResult": "----------------PASS-------------------"
      },
      {
        "expectedBodyPath": "spec.emails",
        "expectedBodyValue": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ],
        "receivedBodyValue": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ],
        "bodyPathValueCheckResult": "----------------PASS-------------------"
      },
      {
        "expectedBodyPath": "spec.age",
        "expectedBodyValue": 33,
        "receivedBodyValue": 33,
        "bodyPathValueCheckResult": "----------------PASS-------------------"
      },
      {
        "expectedBodyPath": "spec.active",
        "expectedBodyValue": true,
        "receivedBodyValue": true,
        "bodyPathValueCheckResult": "----------------PASS-------------------"
      }
    ],
    "expectedBody": {
      "name": "JSON-POST-TEST",
      "spec": {
        "name": "Alex",
        "age": 33,
        "active": true,
        "emails": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ]
      }
    },
    "comparedBody": {
      "name": "JSON-POST-TEST",
      "spec": {
        "name": "Alex",
        "age": 33,
        "active": true,
        "emails": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ]
      }
    },
    "bodyCheckResult": "----------------PASS-------------------",
    "receivedBody": {
      "id": 2,
      "name": "JSON-POST-TEST",
      "spec": {
        "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
        "method": "POST",
        "name": "Alex",
        "age": 33,
        "active": true,
        "emails": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ]
      },
      "createdAt": "2025-08-12T21:24:06"
    }
  },
  "startedAt": "2025-08-12T21:24:05",
  "finishedAt": "2025-08-12T21:24:06"
}
```

---
## ❌ Example of JSON formatted fail test specification
```json
{
  "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer abc123",
    "X-Custom-Header": "test"
  },
  "body": {
    "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST-FOR-FAIL",
    "method": "POST",
    "name": "Alex1",
    "age": "33",
    "active": "true",
    "emails": [
      "alexandr.bezarov@gmail.com@gmail.com",
      "bezarov.sasha1@gmail.com"
    ]
  },
  "expectedStatusCode": 404,
  "expectedContentType": "application/text",
  "expectedBody": {
    "id": 1,
    "name": "JSON-POST-TEST",
    "spec": {
      "name": "Alex",
      "age": 33,
      "active": true,
      "emails": [
        "alexandr.bezarov@gmail.com",
        "bezarov.sasha@gmail.com"
      ]
    },
    "createdAt": "2025-07-27T14:28:18"
  },
  "excludedBodyFields": [
    "id",
    "createdAt"
  ],
  "excludeAllOtherBodyFields": false,
  "expectedBodyPaths": [
    {
      "expectedBodyPath": "spec.name",
      "expectedBodyValue": "Alex"
    },
    {
      "expectedBodyPath": "spec.emails",
      "expectedBodyValue": [
        "alexandr.bezarov@gmail.com",
        "bezarov.sasha@gmail.com"
      ]
    },
    {
      "expectedBodyPath": "spec.age",
      "expectedBodyValue": 33
    },
    {
      "expectedBodyPath": "spec.active",
      "expectedBodyValue": true
    }
  ]
}
```

## ❌ Example of JSON formatted fail test run log

```json
{
  "runId": 1,
  "specId": 1,
  "overallTestStatus": "----------------FAIL-------------------",
  "testResultLog": {
    "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST",
    "method": "POST",
    "expectedStatusCode": 404,
    "receivedStatusCode": 200,
    "statusCodeCheckResult": "----------------FAIL-------------------",
    "expectedContentType": "application/text",
    "receivedContentType": "application/json",
    "contentTypeCheckResult": "----------------FAIL-------------------",
    "expectedBodyPathCheck": [
      {
        "expectedBodyPath": "spec.name",
        "expectedBodyValue": "Alex",
        "receivedBodyValue": "Alex1",
        "bodyPathValueCheckResult": "----------------FAIL-------------------"
      },
      {
        "expectedBodyPath": "spec.emails",
        "expectedBodyValue": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ],
        "receivedBodyValue": [
          "alexandr.bezarov@gmail.com@gmail.com",
          "bezarov.sasha1@gmail.com"
        ],
        "bodyPathValueCheckResult": "----------------FAIL-------------------"
      },
      {
        "expectedBodyPath": "spec.age",
        "expectedBodyValue": 33,
        "receivedBodyValue": "33",
        "bodyPathValueCheckResult": "----------------FAIL-------------------"
      },
      {
        "expectedBodyPath": "spec.active",
        "expectedBodyValue": true,
        "receivedBodyValue": "true",
        "bodyPathValueCheckResult": "----------------FAIL-------------------"
      }
    ],
    "expectedBody": {
      "name": "JSON-POST-TEST",
      "spec": {
        "name": "Alex",
        "age": 33,
        "active": true,
        "emails": [
          "alexandr.bezarov@gmail.com",
          "bezarov.sasha@gmail.com"
        ]
      }
    },
    "comparedBody": {
      "name": "JSON-POST-TEST",
      "spec": {
        "name": "Alex1",
        "age": "33",
        "active": "true",
        "emails": [
          "alexandr.bezarov@gmail.com@gmail.com",
          "bezarov.sasha1@gmail.com"
        ]
      }
    },
    "bodyCheckResult": "----------------FAIL-------------------",
    "receivedBody": {
      "id": 2,
      "name": "JSON-POST-TEST",
      "spec": {
        "url": "http://localhost:8080/test/spec/create?specName=JSON-POST-TEST-FOR-FAIL",
        "method": "POST",
        "name": "Alex1",
        "age": "33",
        "active": "true",
        "emails": [
          "alexandr.bezarov@gmail.com@gmail.com",
          "bezarov.sasha1@gmail.com"
        ]
      },
      "createdAt": "2025-08-12T21:40:57"
    }
  },
  "startedAt": "2025-08-12T21:40:56",
  "finishedAt": "2025-08-12T21:40:57"
}
```
## ✅ Example of XML formatted success test specification
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<root>
    <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
    <method>POST</method>
    <headers>
        <Authorization>Bearer abc123</Authorization>
        <X-Custom-Header>test</X-Custom-Header>
    </headers>
    <body>
        <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
        <method>POST</method>
        <name>Alex</name>
        <age>33</age>
        <active>true</active>
        <emails>alexandr.bezarov@gmail.com</emails>
        <emails>bezarov.sasha@gmail.com</emails>
    </body>
    <expectedStatusCode>200</expectedStatusCode>
    <expectedContentType>application/xml</expectedContentType>
    <expectedBody>
        <id>1</id>
        <name>XML-POST-TEST</name>
        <spec>
            <name>Alex</name>
            <age>33</age>
            <active>true</active>
            <emails>alexandr.bezarov@gmail.com</emails>
            <emails>bezarov.sasha@gmail.com</emails>
        </spec>
        <createdAt>2025-07-27T14:28:18</createdAt>
    </expectedBody>
    <excludedBodyFields>id</excludedBodyFields>
    <excludedBodyFields>createdAt</excludedBodyFields>
    <excludeAllOtherBodyFields>false</excludeAllOtherBodyFields>
    <expectedBodyPaths>
        <expectedBodyPath>spec.name</expectedBodyPath>
        <expectedBodyValue>Alex</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.emails</expectedBodyPath>
        <expectedBodyValue>alexandr.bezarov@gmail.com</expectedBodyValue>
        <expectedBodyValue>bezarov.sasha@gmail.com</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.age</expectedBodyPath>
        <expectedBodyValue>33</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.active</expectedBodyPath>
        <expectedBodyValue>true</expectedBodyValue>
    </expectedBodyPaths>
</root>
```

## ✅ Example of XML formatted success test run log

```xml
<TestRunDTO>
    <runId>3</runId>
    <specId>5</specId>
    <overallTestStatus>----------------PASS-------------------</overallTestStatus>
    <testResultLog>
        <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
        <method>POST</method>
        <expectedStatusCode>200</expectedStatusCode>
        <receivedStatusCode>200</receivedStatusCode>
        <statusCodeCheckResult>----------------PASS-------------------</statusCodeCheckResult>
        <expectedContentType>application/xml</expectedContentType>
        <receivedContentType>application/xml</receivedContentType>
        <contentTypeCheckResult>----------------PASS-------------------</contentTypeCheckResult>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.name</expectedBodyPath>
            <expectedBodyValue>Alex</expectedBodyValue>
            <receivedBodyValue>Alex</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.emails</expectedBodyPath>
            <expectedBodyValue>alexandr.bezarov@gmail.com</expectedBodyValue>
            <expectedBodyValue>bezarov.sasha@gmail.com</expectedBodyValue>
            <receivedBodyValue>alexandr.bezarov@gmail.com</receivedBodyValue>
            <receivedBodyValue>bezarov.sasha@gmail.com</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.age</expectedBodyPath>
            <expectedBodyValue>33</expectedBodyValue>
            <receivedBodyValue>33</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.active</expectedBodyPath>
            <expectedBodyValue>true</expectedBodyValue>
            <receivedBodyValue>true</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBody>
            <name>XML-POST-TEST</name>
            <spec>
                <name>Alex</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com</emails>
                <emails>bezarov.sasha@gmail.com</emails>
            </spec>
        </expectedBody>
        <comparedBody>
            <name>XML-POST-TEST</name>
            <spec>
                <name>Alex</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com</emails>
                <emails>bezarov.sasha@gmail.com</emails>
            </spec>
        </comparedBody>
        <bodyCheckResult>----------------PASS-------------------</bodyCheckResult>
        <receivedBody>
            <id>6</id>
            <name>XML-POST-TEST</name>
            <spec>
                <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
                <method>POST</method>
                <name>Alex</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com</emails>
                <emails>bezarov.sasha@gmail.com</emails>
            </spec>
            <createdAt>2025-08-12T21:26:04</createdAt>
        </receivedBody>
    </testResultLog>
    <startedAt>2025-08-12T21:26:04</startedAt>
    <finishedAt>2025-08-12T21:26:04</finishedAt>
</TestRunDTO>
```
## ❌ Example of XML formatted fail test run log

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<root>
    <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
    <method>POST</method>
    <headers>
        <Authorization>Bearer abc123</Authorization>
        <X-Custom-Header>test</X-Custom-Header>
    </headers>
    <body>
        <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST-FOR-FAIL</url>
        <method>POST</method>
        <name>Alex1</name>
        <age>33</age>
        <active>true</active>
        <emails>alexandr.bezarov@gmail.com@gmail.com</emails>
        <emails>bezarov.sasha1@gmail.com</emails>
    </body>
    <expectedStatusCode>404</expectedStatusCode>
    <expectedContentType>application/text</expectedContentType>
    <expectedBody>
        <id>1</id>
        <name>JSON-POST-TEST</name>
        <spec>
            <name>Alex</name>
            <age>33</age>
            <active>true</active>
            <emails>alexandr.bezarov@gmail.com</emails>
            <emails>bezarov.sasha@gmail.com</emails>
        </spec>
        <createdAt>2025-07-27T14:28:18</createdAt>
    </expectedBody>
    <excludedBodyFields>id</excludedBodyFields>
    <excludedBodyFields>createdAt</excludedBodyFields>
    <excludeAllOtherBodyFields>false</excludeAllOtherBodyFields>
    <expectedBodyPaths>
        <expectedBodyPath>spec.name</expectedBodyPath>
        <expectedBodyValue>Alex</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.emails</expectedBodyPath>
        <expectedBodyValue>alexandr.bezarov@gmail.com</expectedBodyValue>
        <expectedBodyValue>bezarov.sasha@gmail.com</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.age</expectedBodyPath>
        <expectedBodyValue>33</expectedBodyValue>
    </expectedBodyPaths>
    <expectedBodyPaths>
        <expectedBodyPath>spec.active</expectedBodyPath>
        <expectedBodyValue>true</expectedBodyValue>
    </expectedBodyPaths>
</root>
```

## ❌ Example of XML formatted fail test specification
```xml
<TestRunDTO>
    <runId>1</runId>
    <specId>1</specId>
    <overallTestStatus>----------------FAIL-------------------</overallTestStatus>
    <testResultLog>
        <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST</url>
        <method>POST</method>
        <expectedStatusCode>404</expectedStatusCode>
        <receivedStatusCode>200</receivedStatusCode>
        <statusCodeCheckResult>----------------FAIL-------------------</statusCodeCheckResult>
        <expectedContentType>application/text</expectedContentType>
        <receivedContentType>application/xml</receivedContentType>
        <contentTypeCheckResult>----------------FAIL-------------------</contentTypeCheckResult>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.name</expectedBodyPath>
            <expectedBodyValue>Alex</expectedBodyValue>
            <receivedBodyValue>Alex1</receivedBodyValue>
            <bodyPathValueCheckResult>----------------FAIL-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.emails</expectedBodyPath>
            <expectedBodyValue>alexandr.bezarov@gmail.com</expectedBodyValue>
            <expectedBodyValue>bezarov.sasha@gmail.com</expectedBodyValue>
            <receivedBodyValue>alexandr.bezarov@gmail.com@gmail.com</receivedBodyValue>
            <receivedBodyValue>bezarov.sasha1@gmail.com</receivedBodyValue>
            <bodyPathValueCheckResult>----------------FAIL-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.age</expectedBodyPath>
            <expectedBodyValue>33</expectedBodyValue>
            <receivedBodyValue>33</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBodyPathCheck>
            <expectedBodyPath>spec.active</expectedBodyPath>
            <expectedBodyValue>true</expectedBodyValue>
            <receivedBodyValue>true</receivedBodyValue>
            <bodyPathValueCheckResult>----------------PASS-------------------</bodyPathValueCheckResult>
        </expectedBodyPathCheck>
        <expectedBody>
            <name>JSON-POST-TEST</name>
            <spec>
                <name>Alex</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com</emails>
                <emails>bezarov.sasha@gmail.com</emails>
            </spec>
        </expectedBody>
        <comparedBody>
            <name>XML-POST-TEST</name>
            <spec>
                <name>Alex1</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com@gmail.com</emails>
                <emails>bezarov.sasha1@gmail.com</emails>
            </spec>
        </comparedBody>
        <bodyCheckResult>----------------FAIL-------------------</bodyCheckResult>
        <receivedBody>
            <id>2</id>
            <name>XML-POST-TEST</name>
            <spec>
                <url>http://localhost:8080/test/spec/create?specName=XML-POST-TEST-FOR-FAIL</url>
                <method>POST</method>
                <name>Alex1</name>
                <age>33</age>
                <active>true</active>
                <emails>alexandr.bezarov@gmail.com@gmail.com</emails>
                <emails>bezarov.sasha1@gmail.com</emails>
            </spec>
            <createdAt>2025-08-12T21:49:05</createdAt>
        </receivedBody>
    </testResultLog>
    <startedAt>2025-08-12T21:49:04</startedAt>
    <finishedAt>2025-08-12T21:49:05</finishedAt>
</TestRunDTO>
```
---

## 🎯 TODO List for Future Improvements

- 🔒 Add authentication and role-based access control
- 📚 Provide Swagger/OpenAPI documentation with live examples
- 🖥️ Build a simple UI dashboard to monitor queues and results
- 🧩 Add more matchers and check handlers (XML, schema validation, etc.)

---

## 🧑‍💻 Contributing
- Feel free to fork, extend and open PRs!
- Add support for more spec formats (XML?)
- Ideas: more matchers, authentication flows, CI/CD integration, dashboards, cloud-native execution.
