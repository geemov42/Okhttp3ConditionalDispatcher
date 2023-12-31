package io.geemov42.okhttp3.conditionaldispatcher.response;

import lombok.Builder;
import lombok.Data;
import okhttp3.mockwebserver.MockResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.geemov42.okhttp3.conditionaldispatcher.utils.StringUtils.requireNonNullAndNotBlank;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

@Data
public class ConditionalMockResponse {

    private String id;
    private int fetchCounter = 0;
    @Builder.Default
    private int limitFetch = -1;

    private Pattern pathRegexPattern;
    private MockResponse mockResponse;
    @Builder.Default
    private List<MatchingCondition> matchingConditions = new ArrayList<>();

    @Builder
    public ConditionalMockResponse(String id, int limitFetch, String pathRegex, MockResponse mockResponse, List<MatchingCondition> matchingConditions) {

        this.id = requireNonNullAndNotBlank(id);
        requireNonNullAndNotBlank(pathRegex);
        this.pathRegexPattern = Pattern.compile(pathRegex);
        this.mockResponse = requireNonNull(mockResponse).clone();
        this.matchingConditions = requireNonNullElseGet(matchingConditions, ArrayList::new);
        this.limitFetch = limitFetch;
    }

    public void use() {
        this.fetchCounter++;
    }

    public MockResponse getMockResponse() {
        this.use();
        return this.mockResponse;
    }

    public boolean isBeyondOfLimit() {
        return this.limitFetch > 0 && this.fetchCounter > this.limitFetch;
    }

    public ConditionalMockResponse addCondition(MatchingCondition matchingCondition) {

        requireNonNull(matchingCondition);
        this.matchingConditions.add(matchingCondition);

        return this;
    }

    public ConditionalMockResponse addConditions(MatchingCondition... matchingConditions) {
        requireNonNull(matchingConditions);

        for (MatchingCondition matchingCondition : matchingConditions) {
            this.addCondition(matchingCondition);
        }

        return this;
    }
}