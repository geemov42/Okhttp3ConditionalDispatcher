package io.geemov42.okhttp3.conditionaldispatcher.response;

import lombok.Builder;
import lombok.Data;
import okhttp3.mockwebserver.MockResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static io.geemov42.okhttp3.conditionaldispatcher.utils.StringUtils.requireNonNullAndNotBlank;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

@Data
public class ConditionalMockResponse {
    private Pattern pathRegexPattern;
    private MockResponse mockResponse;
    @Builder.Default
    private List<MatchingCondition> matchingConditions = new ArrayList<>();

    @Builder
    public ConditionalMockResponse(String pathRegex, MockResponse mockResponse, List<MatchingCondition> matchingConditions) {

        requireNonNullAndNotBlank(pathRegex);
        this.pathRegexPattern = Pattern.compile(pathRegex);
        this.mockResponse = requireNonNull(mockResponse).clone();
        this.matchingConditions = requireNonNullElseGet(matchingConditions, Collections::emptyList);
    }
}