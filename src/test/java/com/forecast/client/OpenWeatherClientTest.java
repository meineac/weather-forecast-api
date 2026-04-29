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

    @Test
    void getForecast_Success() {
        String jsonResponse = """
                {
                    "list": [
                        {
                            "dt": 1682424000,
                            "main": {
                                "temp_min": 8.5,
                                "temp_max": 14.2
                            }
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://mock-openweather.com/forecast?appid=test-api-key&" +
                        "lat=53.9006&lon=27.5590&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ForecastWeather result = client.getForecast(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        assertEquals(1, result.getDays().size());
        assertEquals(new BigDecimal("8.5"), result.getDays().getFirst().getMinTemperature());
        assertEquals(new BigDecimal("14.2"), result.getDays().getFirst().getMaxTemperature());
        assertEquals(LocalDate.ofEpochDay(1682424000L / 86400L), result.getDays().getFirst().getDate());
        mockServer.verify();
    }

    @Test
    void getForecast_AggregatesMultipleBlocksPerDay() {
        String jsonResponse = """
                {
                  "list": [
                    {
                      "dt": 1777453200,
                      "main": { "temp_min": 5.46, "temp_max": 5.46 }
                    },
                    {
                      "dt": 1777464000,
                      "main": { "temp_min": 5.97, "temp_max": 6.99 }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("http://mock-openweather.com/forecast?appid=test-api-key&" +
                        "lat=53.9006&lon=27.5590&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ForecastWeather result = client.getForecast(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        assertEquals(1, result.getDays().size());
        assertEquals(LocalDate.ofEpochDay(1777453200L / 86400L), result.getDays().getFirst().getDate());
        assertEquals(new BigDecimal("5.46"), result.getDays().getFirst().getMinTemperature());
        assertEquals(new BigDecimal("6.99"), result.getDays().getFirst().getMaxTemperature());

        mockServer.verify();
    }

    @Test
    void getForecast_EmptyList() {
        String jsonResponse = """
                {
                  "list": [
                  ]
                }
                """;

        mockServer.expect(requestTo("http://mock-openweather.com/forecast?appid=test-api-key&" +
                        "lat=53.9006&lon=27.5590&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                client.getForecast(new BigDecimal("53.9006"), new BigDecimal("27.5590")));

        assertEquals("failed to decode response: missing forecast list", exception.getMessage());

        mockServer.verify();
    }
}
