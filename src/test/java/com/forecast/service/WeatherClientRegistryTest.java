package com.forecast.service;

import com.forecast.client.WeatherDataClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WeatherClientRegistryTest {
    static private WeatherClientRegistry registry;

    private static WeatherDataClient stubClient(String name) {
        return new WeatherDataClient() {
            @Override public String getProviderName() { return name; }
            @Override public BigDecimal getCurrentTemperature(BigDecimal lat, BigDecimal lon) {
                return BigDecimal.ZERO;
            }
        };
    }

    @BeforeAll
    static void setUp() {
        registry = new WeatherClientRegistry(List.of(
                stubClient("openweather"),
                stubClient("weatherapi")
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"openweather", "weatherapi"})
    void get_KnownProviders(String providerName) {
        WeatherDataClient client = registry.get(providerName);
        assertEquals(providerName, client.getProviderName());
    }

    @Test
    void get_UnknownProvider() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> registry.get("nonexistent")
        );
        assertEquals("Unknown provider: nonexistent", ex.getMessage());
    }

    @Test
    void get_NullProvider() {
        assertThrows(IllegalArgumentException.class, () -> registry.get(null));
    }

    @Test
    void constructor_DuplicateProviderNames() {
        assertThrows(IllegalArgumentException.class, () ->
                new WeatherClientRegistry(List.of(
                        stubClient("openweather"),
                        stubClient("openweather")
                ))
        );
    }
}
