package com.example.spectestengine.utils;

import java.util.Set;

public final class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
    public static final Set<String> MANDATORY_FIELDS = Set.of("url", "method");

    public static final int MAX_QUEUE_SIZE = 1000;

    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String BODY = "body";
    public static final String HEADERS = "headers";

    public static final String EXPECTED_STATUS_CODE = "expectedStatusCode";
    public static final String RECEIVED_STATUS_CODE = "receivedStatusCode";
    public static final String STATUS_CODE_CHECK_RESULT = "statusCodeCheckResult";

    public static final String EXCEPTED_MEDIA_TYPE = "expectedMediaType";
    public static final String RECEIVED_MEDIA_TYPE = "receivedMediaType";
    public static final String MEDIA_TYPE_CHECK_RESULT = "mediaTypeCheckResult";

    public static final String EXCEPTED_BODY_JSON_PATHS = "expectedBodyJsonPaths";
    public static final String EXPECTED_JSON_PATH = "expectedJsonPath";
    public static final String EXPECTED_JSON_VALUE = "expectedJsonValue";
    public static final String EXPECTED_JSON_PATH_CHECK = "expectedJsonPathCheck";
    public static final String RECEIVED_JSON_VALUE = "receivedJsonValue";
    public static final String BODY_JSON_PATH_VALUE_CHECK_RESULT = "bodyJsonPathValueCheckResult";

    public static final String EXPECTED_BODY = "expectedBody";
    public static final String COMPARED_BODY = "comparedBody";
    public static final String EXCLUDED_BODY_FIELDS = "excludedBodyFields";
    public static final String EXCLUDE_ALL_OTHER_BODY_FIELDS = "excludeAllOtherBodyFields";
    public static final String RECEIVED_BODY = "receivedBody";
    public static final String BODY_CHECK_RESULT = "bodyCheckResult";
    public static final String BODY_CHECK_ERROR = "bodyCheckError";

    public static final String TEST_RUN_ERROR = "------------TEST RUN ERROR-------------";
    public static final String PASS = "----------------PASS-------------------";
    public static final String FAIL = "----------------FAIL-------------------";
    public static final String ERROR = "---------------ERROR-------------------";
}
