package com.example.spectestengine.utils;

import java.util.Set;

public final class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int MAX_QUEUE_SIZE = 1000;

    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String HEADERS = "headers";
    public static final String BODY = "body";

    public static final Set<String> MANDATORY_FIELDS = Set.of(URL, METHOD);
    public static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    public static final String EXPECTED_STATUS_CODE = "expectedStatusCode";
    public static final String RECEIVED_STATUS_CODE = "receivedStatusCode";
    public static final String STATUS_CODE_CHECK_RESULT = "statusCodeCheckResult";

    public static final String EXCEPTED_CONTENT_TYPE = "expectedContentType";
    public static final String RECEIVED_CONTENT_TYPE = "receivedContentType";
    public static final String CONTENT_TYPE_CHECK_RESULT = "contentTypeCheckResult";

    public static final String EXCEPTED_BODY_PATHS = "expectedBodyPaths";
    public static final String EXPECTED_BODY_PATH = "expectedBodyPath";
    public static final String EXPECTED_BODY_VALUE = "expectedBodyValue";
    public static final String EXPECTED_BODY_PATH_CHECK = "expectedBodyPathCheck";
    public static final String RECEIVED_BODY_VALUE = "receivedBodyValue";
    public static final String BODY_PATH_VALUE_CHECK_RESULT = "bodyPathValueCheckResult";

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
