package io.geemov42.okhttp3.conditionaldispatcher;

import io.geemov42.okhttp3.conditionaldispatcher.enums.HttpMethodEnum;
import io.geemov42.okhttp3.conditionaldispatcher.response.MatchingCondition;
import io.geemov42.okhttp3.conditionaldispatcher.response.ConditionalMockResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.*;
import java.util.stream.Collectors;

import static io.geemov42.okhttp3.conditionaldispatcher.enums.HttpMethodEnum.COMMON;
import static io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum.*;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * @author geemov42
 * Transportable class to add mock response with conditions
 */
@Slf4j
public class ConditionalDispatcher extends Dispatcher {

    private QueueDispatcher queueDispatcher = new QueueDispatcher();
    private final Map<HttpMethodEnum, List<ConditionalMockResponse>> mockResponseMap = new EnumMap<>(HttpMethodEnum.class);

    @Override
    public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {

        if (isNull(recordedRequest.getMethod())) {
            return this.queueDispatcher.dispatch(recordedRequest);
        }

        HttpMethodEnum requestHttpMethod = HttpMethodEnum.valueOf(recordedRequest.getMethod().toUpperCase());

        if (!this.mockResponseMap.containsKey(requestHttpMethod) && !this.mockResponseMap.containsKey(COMMON)) {
            return this.queueDispatcher.dispatch(recordedRequest);
        }

        Optional<MockResponse> mockResponseOptional = this.findMockResponseForRequest(requestHttpMethod, recordedRequest);
        if (mockResponseOptional.isPresent()) {
            return mockResponseOptional.get();
        }

        mockResponseOptional = this.findMockResponseForRequest(COMMON, recordedRequest);
        if (mockResponseOptional.isPresent()) {
            return mockResponseOptional.get();
        }

        return this.queueDispatcher.dispatch(recordedRequest);
    }

    private Optional<MockResponse> findMockResponseForRequest(HttpMethodEnum methodDispatcher, RecordedRequest recordedRequest) {

        if (!this.mockResponseMap.containsKey(methodDispatcher) || isNull(recordedRequest.getPath())) {
            return Optional.empty();
        }

        List<ConditionalMockResponse> conditionalMockResponses = this.mockResponseMap.get(methodDispatcher);

        return conditionalMockResponses.stream()
                .filter(conditionalMockResponse -> conditionalMockResponse.getPathRegexPattern().matcher(recordedRequest.getPath()).find())
                .filter(conditionalMockResponse -> {
                    for (MatchingCondition matchingCondition : conditionalMockResponse.getMatchingConditions()) {
                        if (!this.matchCondition(recordedRequest, matchingCondition)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(conditionalMockResponse -> {
                    MockResponse mockResponse = conditionalMockResponse.getMockResponse();

                    if (conditionalMockResponse.isBeyondOfLimit()) {
                        log.error("[{}] {} is beyond the limit define {}/{}",
                                methodDispatcher,
                                conditionalMockResponse.getId(),
                                conditionalMockResponse.getFetchCounter(),
                                conditionalMockResponse.getLimitFetch()
                        );
                    }

                    return mockResponse;
                })
                .findFirst();
    }

    private boolean matchCondition(RecordedRequest recordedRequest, MatchingCondition matchingCondition) {
        try {
            if (matchingCondition.getRequestPartToTest() == HEADER) {
                String headerField = this.getHeaderValue(recordedRequest, matchingCondition.getField());
                return matchingCondition.getValuePattern().matcher(headerField).find();
            }

            if (matchingCondition.getRequestPartToTest() == PARAMETER) {
                String requestParameterValue = this.getRequestParameterValue(recordedRequest, matchingCondition.getField());
                return matchingCondition.getValuePattern().matcher(requestParameterValue).find();
            }

            if (matchingCondition.getRequestPartToTest() == BODY) {
                return matchingCondition.getValuePattern().matcher(recordedRequest.getBody().toString()).find();
            }

        } catch (NullPointerException ignored) {
            return false;
        }

        return false;
    }

    private String getHeaderValue(RecordedRequest recordedRequest, String field) {

        return requireNonNull(recordedRequest.getHeader(field));
    }

    private String getRequestParameterValue(RecordedRequest recordedRequest, String field) {

        requireNonNull(recordedRequest.getRequestUrl());
        return requireNonNull(recordedRequest.getRequestUrl().queryParameter(field));
    }

    public ConditionalDispatcher addResponseForMethod(HttpMethodEnum methodDispatcher, List<ConditionalMockResponse> conditionalMockResponses) {

        if (isNull(methodDispatcher) || isNull(conditionalMockResponses)) {
            return this;
        }

        conditionalMockResponses = conditionalMockResponses.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (conditionalMockResponses.isEmpty()) {
            return this;
        }

        List<ConditionalMockResponse> internConditionalMockResponses = this.mockResponseMap.get(methodDispatcher);

        if (isNull(internConditionalMockResponses)) {

            internConditionalMockResponses = new ArrayList<>();
            this.mockResponseMap.put(methodDispatcher, internConditionalMockResponses);
        }

        internConditionalMockResponses.addAll(conditionalMockResponses);

        return this;
    }

    public ConditionalDispatcher addResponse(List<ConditionalMockResponse> conditionalMockResponses) {

        return this.addResponseForMethod(COMMON, conditionalMockResponses);
    }

    public ConditionalDispatcher addResponseInQueue(MockResponse mockResponse) {

        this.queueDispatcher.enqueueResponse(mockResponse.clone());
        return this;
    }

    /**
     * This method will help you to obtain conditional mock response to analyze the statistic
     * @param httpMethodEnum
     * @return
     */
    public Map<String, ConditionalMockResponse> getConditionalMockResponseMapForMethod(HttpMethodEnum httpMethodEnum) {

        requireNonNull(httpMethodEnum);

        List<ConditionalMockResponse> conditionalMockResponses = this.mockResponseMap.get(httpMethodEnum);

        if (isNull(conditionalMockResponses)) {
            return Collections.emptyMap();
        }

        return conditionalMockResponses.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ConditionalMockResponse::getId, conditionalMockResponse -> conditionalMockResponse));
    }

    public void resetResponseQueue() {
        this.queueDispatcher = new QueueDispatcher();
    }
}
