package com.forecast.client;

import com.forecast.model.ForecastWeather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class GoogleWeatherClientTest {

    private MockRestServiceServer mockServer;
    private GoogleWeatherClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("http://mock-googleweather.com")
                .defaultUriVariables(Map.of("apiKey", "test-api-key"))
                .build();

        client = new GoogleWeatherClient(restClient);
    }

    @Test
    void getProviderName_ReturnsGoogleWeather() {
        assertEquals("googleweather", client.getProviderName());
    }

    @Test
    void getCurrentTemperature_Success() {
        String jsonResponse = """
                {
                    "temperature": {
                        "degrees": 13.7
                    }
                }
                """;

        mockServer.expect(requestTo("http://mock-googleweather.com/currentConditions:lookup?key=test-api-key&" +
                        "location.latitude=53.9006&location.longitude=27.5590"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        BigDecimal result = client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        assertEquals(new BigDecimal("13.7"), result);
        mockServer.verify();
    }

    @Test
    void getCurrentTemperature_ReturnsHttpError() {
        mockServer.expect(requestTo("http://mock-googleweather.com/currentConditions:lookup?key=test-api-key&" +
                        "location.latitude=53.9006&location.longitude=27.5590"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        assertEquals("googleweather returned bad status: 403 FORBIDDEN", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void getCurrentTemperature_MissingDegreesField() {
        String jsonResponse = """
                {
                    "temperature": {
                        "unit": "CELSIUS"
                    }
                }
                """;

        mockServer.expect(requestTo("http://mock-googleweather.com/currentConditions:lookup?key=test-api-key&" +
                        "location.latitude=53.9006&location.longitude=27.5590"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                client.getCurrentTemperature(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        assertEquals("failed to decode response: missing temperature data", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void getForecast_Success() {
        String jsonResponse = """
                {
                    "forecastDays": [
                        {
                            "displayDate": {
                                "year": 2026,
                                "month": 4,
                                "day": 30
                            },
                            "maxTemperature": {
                                "degrees": 10.1,
                                "unit": "CELSIUS"
                            },
                            "minTemperature": {
                                "degrees": 2,
                                "unit": "CELSIUS"
                            }
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://mock-googleweather.com/forecast/days:lookup?" +
                "key=test-api-key&location.latitude=53.9006&location.longitude=27.5590"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ForecastWeather result = client.getForecast(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        assertEquals(1, result.getDays().size());
        assertEquals(new BigDecimal("2"), result.getDays().getFirst().getMinTemperature());
        assertEquals(new BigDecimal("10.1"), result.getDays().getFirst().getMaxTemperature());
        assertEquals(LocalDate.of(2026, 4, 30), result.getDays().getFirst().getDate());
        mockServer.verify();
    }
}
