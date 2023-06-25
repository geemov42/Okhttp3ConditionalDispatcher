# Global note

Feel free to clone and install to your repository.
This library will not be installed on maven central because of no maintenance.
It's a one shot.

# Goal

The goal of this library is to have mock http response based on condition for an external service.
Okhttp3 support out of the box the queue dispatcher that dispatch simply response in fifo mode.

this dispatcher complete the offer and give the opportunity to respond to dynamic calls.

# Usage

How it works :
* The dispatcher will search in the more specific list (based on http method)
* If the request don't match response conditions, the dispatcher search in common list
* If the request don't match response conditions, it uses the queueDispatcher to answer

# Code

## initialization

This is the same way of okHttp3 :

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
```

## Conditional dispatcher instance creation

```java
ConditionalDispatcher conditionalDispatcher = new ConditionalDispatcher();
```

it's a simple constructor

## Conditional response

It exists 3 methods to filled the dispatcher with response :
* addResponseForMethod (Add response in method specific list)
* addResponse (Add response in a common list)
* addResponseInQueue (Use the queue dispatcher of okHttp3)

To create a conditional response :

```java
// The normal okHttp3 mock response
MockResponse mockedResponse = new MockResponse()
        .setBody(new Gson().toJson(Map.of(
                "personIdentifier", ssin,
                "hasConsent", true
        ))) //Sample
        .addHeader("Content-Type", "application/json");

// The conditional mock response with helper methods
ConditionalMockResponse definition = conditionalMockResponse(uniqueId, "\\/hasConsent", mockedResponse, 1)
        .addCondition(param("personIdentifier", paramRegex));

// Add response for GET method
conditionalDispatcher.addResponseForMethod(GET, List.of(definition));
```

## Assertion

It is important to be sure that the external service is called like we want.
To be sure of external call, you can assert on conditional mock response fetch count :

```java
// We can assert on conditional response fetching. You can specify a limit you can use to be sure calls are really waiting for
Map<String, ConditionalMockResponse> getMockResponse = conditionalDispatcher.getConditionalMockResponseMapForMethod(GET);
Assertions.assertSame(1, getMockResponse.get("get_hasConsent").getFetchCounter());
```
