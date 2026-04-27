package com.forecast.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;



class OpenWeatherClientTest {

    private MockRestServiceServer mockServer;
    private OpenWeatherClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("http://mock-openweather.com")
                .defaultUriVariables(Map.of("apiKey", "test-api-key"))
                .build();

        client = new OpenWeatherClient(restClient);
    }

    @Test
    void getCurrentTemperature_Success() {
        String jsonResponse = """
                {
                    "main": {
                        "temp": 15.5
                    }
                }
                """;

        mockServer.expect(requestTo("http://mock-openweather.com/weather?appid=test-api-key&lat=53.9006" +
                        "&lon=27.5590&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        BigDecimal result = client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        assertEquals(new BigDecimal("15.5"), result);
        mockServer.verify();
    }

    @Test
    void getCurrentTemperature_ReturnsHttpError() {
        mockServer.expect(requestTo("http://mock-openweather.com/weather?appid=test-api-key&lat=53.9006" +
                        "&lon=27.5590&units=metric"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        assertEquals("openweather returned bad status: 401 UNAUTHORIZED", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void getCurrentTemperature_MalformedJson() {
        mockServer.expect(requestTo("http://mock-openweather.com/weather?appid=test-api-key&lat=53.9006" +
                        "&lon=27.5590&units=metric"))
                .andRespond(withSuccess("invalid json", MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class, () ->
                client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        mockServer.verify();
    }

    @Test
    void getCurrentTemperature_MissingTempField() {
        String jsonResponse = """
                {
                    "main": {
                        "pressure": 1012
                    }
                }
                """;

        mockServer.expect(requestTo("http://mock-openweather.com/weather?appid=test-api-key&lat=53.9006" +
                        "&lon=27.5590&units=metric"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        assertEquals("failed to decode response: missing temperature data", exception.getMessage());

        mockServer.verify();
    }
}
