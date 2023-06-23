package io.geemov42.okhttp3.conditionaldispatcher;

import com.google.gson.Gson;
import io.geemov42.okhttp3.conditionaldispatcher.response.ConditionalMockResponse;
import io.geemov42.okhttp3.conditionaldispatcher.response.MatchingCondition;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.geemov42.okhttp3.conditionaldispatcher.enums.MethodDispatcherEnum.GET;
import static io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum.PARAMETER;

class ConditionalDispatcherTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void shouldReturnAResponseFromGetList_whenSendRequestThatMatchConditions() throws IOException {

        String ssin = "85047";

        ConditionalDispatcher conditionalDispatcher = new ConditionalDispatcher();
        conditionalDispatcher.addResponseForMethod(GET, List.of(
                this.createConditionalMockResponse(ssin, ssin)
        ));
        this.mockWebServer.setDispatcher(conditionalDispatcher);

        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(this.mockWebServer.url("/hasConsent").toString())).newBuilder();
        urlBuilder.addQueryParameter("personIdentifier", ssin);

        String url = urlBuilder.build().toString();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Map<String, Object> responseMap;

        try (Response response = client.newCall(new Request.Builder()
                        .url(url)
                        .build())
                .execute()) {

            Assertions.assertNotNull(response.body());
            responseMap = new Gson().fromJson(response.body().string(), Map.class);
        }

        Assertions.assertEquals(true, responseMap.get("hasConsent"));
        Assertions.assertEquals(ssin, responseMap.get("personIdentifier"));
    }

    @Test
    void shouldReturnAResponseFromCommonList_whenSendRequestThatMatchConditions() throws IOException {

        String ssin = "85047";

        ConditionalDispatcher conditionalDispatcher = new ConditionalDispatcher();
        conditionalDispatcher.addResponseForMethod(GET, List.of(
                        this.createConditionalMockResponse(ssin, "99989845")
                ))
                .addResponse(List.of(
                        this.createConditionalMockResponse(ssin, ssin)
                ));
        this.mockWebServer.setDispatcher(conditionalDispatcher);

        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(this.mockWebServer.url("/hasConsent").toString())).newBuilder();
        urlBuilder.addQueryParameter("personIdentifier", ssin);

        String url = urlBuilder.build().toString();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Map<String, Object> responseMap;

        try (Response response = client.newCall(new Request.Builder()
                        .url(url)
                        .build())
                .execute()) {

            Assertions.assertNotNull(response.body());
            responseMap = new Gson().fromJson(response.body().string(), Map.class);
        }

        Assertions.assertEquals(true, responseMap.get("hasConsent"));
        Assertions.assertEquals(ssin, responseMap.get("personIdentifier"));
    }

    @Test
    void shouldReturnAResponseFromQueue_whenSendRequestDontMatchConditions() throws IOException {

        String ssin = "85047";

        MockResponse mockedResponse = new MockResponse()
                .setBody(new Gson().toJson(Map.of(
                        "personIdentifier", ssin,
                        "hasConsent", true
                ))) //Sample
                .addHeader("Content-Type", "application/json");

        ConditionalDispatcher conditionalDispatcher = new ConditionalDispatcher();
        conditionalDispatcher.addResponseForMethod(GET, List.of(
                        this.createConditionalMockResponse(ssin, "99989845")
                ))
                .addResponse(List.of(
                        this.createConditionalMockResponse(ssin, "99989845")
                ))
                .addResponseInQueue(mockedResponse);

        this.mockWebServer.setDispatcher(conditionalDispatcher);

        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(this.mockWebServer.url("/hasConsent").toString())).newBuilder();
        urlBuilder.addQueryParameter("personIdentifier", ssin);

        String url = urlBuilder.build().toString();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Map<String, Object> responseMap;

        try (Response response = client.newCall(new Request.Builder()
                        .url(url)
                        .build())
                .execute()) {

            Assertions.assertNotNull(response.body());
            responseMap = new Gson().fromJson(response.body().string(), Map.class);
        }

        Assertions.assertEquals(true, responseMap.get("hasConsent"));
        Assertions.assertEquals(ssin, responseMap.get("personIdentifier"));
    }

    private ConditionalMockResponse createConditionalMockResponse(String ssin, String parameter) {

        MockResponse mockedResponse = new MockResponse()
                .setBody(new Gson().toJson(Map.of(
                        "personIdentifier", ssin,
                        "hasConsent", true
                ))) //Sample
                .addHeader("Content-Type", "application/json");

        return ConditionalMockResponse.builder()
                .pathRegex("\\/hasConsent")
                .mockResponse(mockedResponse)
                .matchingConditions(List.of(
                        MatchingCondition.builder()
                                .requestPartToTest(PARAMETER)
                                .valueRegex(parameter)
                                .field("personIdentifier")
                                .build()
                ))
                .build();
    }
}