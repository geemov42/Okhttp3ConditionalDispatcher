# Usage

How it works :
* The dispatcher will search in the more specific list (GET)
* If not found, it will search in the common list
* If not found, it use the queueDispatcher to answer

```java
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
void shouldReturnAResponseFromQueue_whenSendRequestDontMatchConditions() throws IOException {

    // False value
    String ssin = "85047";

    // Prepare the response
    MockResponse mockedResponse = new MockResponse()
            .setBody(new Gson().toJson(Map.of(
                    "personIdentifier", ssin,
                    "hasConsent", true
            ))) //Sample
            .addHeader("Content-Type", "application/json");

    // Create the dispatcher with 3 levels of response
    ConditionalDispatcher conditionalDispatcher = new ConditionalDispatcher();
    conditionalDispatcher
            // Add response for GET method
            .addResponseForMethod(GET, List.of(
                    this.createConditionalMockResponse(ssin, "99989845")
            ))
            // Add response in a common response list (http method is not important)
            .addResponse(List.of(
                    this.createConditionalMockResponse(ssin, "99989845")
            ))
            // Add response in a queue like a normal situation
            .addResponseInQueue(mockedResponse);
    
    this.mockWebServer.setDispatcher(conditionalDispatcher);

    // We do a request
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

    // Prepare the response
    MockResponse mockedResponse = new MockResponse()
            .setBody(new Gson().toJson(Map.of(
                    "personIdentifier", ssin,
                    "hasConsent", true
            ))) //Sample
            .addHeader("Content-Type", "application/json");

    // Prepare a conditional MockResponse with a condition on the path and another one on a param named personIdentifier
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
```