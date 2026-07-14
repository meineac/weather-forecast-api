# Weather Forecast API

A Spring Boot REST API that aggregates weather data from multiple providers (OpenWeather, Google Weather). Supports current weather, multi-day forecasts, and batch requests for multiple cities — all through a unified interface powered by the **strategy pattern**.

## Tech Stack

- **Java 25** · **Spring Boot 4**
- **RestClient** — HTTP calls to external weather APIs
- **SpringDoc OpenAPI** — interactive API documentation
- **JUnit 5** + **Mockito** — unit testing
- **JaCoCo** — code coverage reporting
- **Lombok**

## Architecture

```
com.forecast
├── client/               # Weather provider adapters (strategy pattern)
│   ├── WeatherProvider.java        # Common interface
│   ├── WeatherDataClient.java      # Current weather port
│   ├── ForecastDataClient.java     # Forecast port
│   ├── OpenWeatherClient.java      # OpenWeather implementation
│   └── GoogleWeatherClient.java    # Google Weather implementation
│
├── config/               # Spring configuration (RestClient beans, Swagger)
├── controller/           # REST endpoints
├── dto/                  # Response wrappers (SuccessResponse<T>, StatusResponse)
├── model/                # Domain models (CurrentWeather, ForecastWeather, Coordinate)
├── properties/           # Externalized config (API keys, city mappings)
└── service/
    ├── WeatherService.java          # Business logic
    ├── WeatherClientRegistry.java   # Provider registry (strategy selector)
    └── LocationResolver.java        # City name → coordinate mapping
```

### Key Design Decisions

- **Strategy Pattern** — weather providers implement a common interface; the `WeatherClientRegistry` resolves the correct provider at runtime. Adding a new weather source requires only implementing the interface and registering a bean.
- **Capability-Based Dispatch** — providers can implement `WeatherDataClient` (current weather), `ForecastDataClient` (forecasts), or both. The service checks at runtime which capabilities a provider supports.
- **Consistent API Response** — all endpoints return `SuccessResponse<T>` or `StatusResponse`, ensuring uniform error handling.

## Features

- **Current Weather** — get temperature by coordinates or city name
- **Forecast** — multi-day weather forecast
- **Batch Requests** — current weather for multiple cities in a single request (parallel processing)
- **Multi-Provider** — switch between weather data sources via query parameter
- **City Resolution** — city names are mapped to coordinates via configuration

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+
- API keys for OpenWeather and/or Google Weather


### Run

```bash
git clone https://github.com/meineac/weather-forecast-api.git
cd weather-forecast-api
./mvnw spring-boot:run
```

### Configuration

Create `.env` file where you provide you API keys

```
cp .env.example .env
```

API docs: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/weather?city=Minsk&provider=openweather` | Current weather by city |
| GET | `/api/v1/weather?lat=53.9&lon=27.5&provider=openweather` | Current weather by coordinates |
| GET | `/api/v1/forecast?city=Minsk&provider=openweather` | Multi-day forecast |
| GET | `/api/v1/weather/batch?cities=Minsk,London&provider=openweather` | Batch current weather |

## Testing

```bash
./mvnw test
```

Unit tests cover the service layer, controller, client adapters, and location resolver with Mockito mocks.