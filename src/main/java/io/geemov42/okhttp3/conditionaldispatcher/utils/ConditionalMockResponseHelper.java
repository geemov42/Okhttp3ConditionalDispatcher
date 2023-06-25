package io.geemov42.okhttp3.conditionaldispatcher.utils;

import io.geemov42.okhttp3.conditionaldispatcher.response.ConditionalMockResponse;
import io.geemov42.okhttp3.conditionaldispatcher.response.MatchingCondition;
import okhttp3.mockwebserver.MockResponse;

import static io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum.*;

public class ConditionalMockResponseHelper {

    private ConditionalMockResponseHelper(){}

    public static MatchingCondition param(String field, String valueRegex) {

        return MatchingCondition.builder()
                .requestPartToTest(PARAMETER)
                .valueRegex(valueRegex)
                .field(field)
                .build();
    }

    public static MatchingCondition header(String field, String valueRegex) {

        return MatchingCondition.builder()
                .requestPartToTest(HEADER)
                .valueRegex(valueRegex)
                .field(field)
                .build();
    }

    public static MatchingCondition body(String valueRegex) {

        return MatchingCondition.builder()
                .requestPartToTest(BODY)
                .valueRegex(valueRegex)
                .build();
    }

    public static ConditionalMockResponse conditionalMockResponse(String uniqueId, String pathRegex, MockResponse mockedResponse) {

        return ConditionalMockResponse.builder()
                .id(uniqueId)
                .pathRegex(pathRegex)
                .mockResponse(mockedResponse)
                .build();
    }

    public static ConditionalMockResponse conditionalMockResponse(String uniqueId, String pathRegex, MockResponse mockedResponse, int limitFetch) {

        return ConditionalMockResponse.builder()
                .id(uniqueId)
                .limitFetch(limitFetch)
                .pathRegex(pathRegex)
                .mockResponse(mockedResponse)
                .build();
    }
}
