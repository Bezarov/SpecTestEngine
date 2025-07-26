package com.example.spectestengine.utils;

public final class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String EXPECTED_STATUS_CODE = "expectedStatusCode";
    public static final String RECEIVED_STATUS_CODE = "receivedStatusCode";
    public static final String STATUS_CODE_CHECK_RESULT = "statusCodeCheckResult";

    public static final String EXCEPTED_MEDIA_TYPE = "expectedMediaType";
    public static final String RECEIVED_MEDIA_TYPE = "receivedMediaType";
    public static final String MEDIA_TYPE_CHECK_RESULT = "mediaTypeCheckResult";

    public static final String EXCEPTED_BODY_JSON_PATH = "expectedBodyJsonPath";
    public static final String EXCEPTED_BODY_JSON_VALUE = "expectedBodyJsonValue";
    public static final String RECEIVED_BODY_JSON_PATH_VALUE = "receivedBodyJsonPathValue";
    public static final String BODY_JSON_PATH_VALUE_CHECK_RESULT = "bodyJsonPathValueCheckResult";

    public static final String EXPECTED_BODY = "expectedBody";
    public static final String COMPARED_BODY = "comparedBody";
    public static final String EXCLUDED_BODY_FIELDS = "excludedBodyFields";
    public static final String EXCLUDE_ALL_OTHER_BODY_FIELDS = "excludeAllOtherBodyFields";
    public static final String RECEIVED_BODY = "receivedBody";
    public static final String BODY_CHECK_RESULT = "bodyCheckResult";
    public static final String BODY_CHECK_ERROR = "bodyCheckError";

    public static final String PASS = "----------------PASS-------------------";
    public static final String FAIL = "----------------FAIL-------------------";
    public static final String ERROR = "---------------ERROR-------------------";
}
