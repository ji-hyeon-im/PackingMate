package com.packingmate.service.weather;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Open-Meteo 지오코딩 및 예보 경계. 사용자 위치는 로그하지 않는다. */
@Component
public class OpenMeteoClient {
    private final RestClient geocodingClient;
    private final RestClient forecastClient;

    public OpenMeteoClient() {
        this.geocodingClient = WeatherRestClientFactory.create("https://geocoding-api.open-meteo.com");
        this.forecastClient = WeatherRestClientFactory.create("https://api.open-meteo.com");
    }

    public Optional<WeatherLocation> locate(String city, String countryCode) {
        JsonNode body = geocodingClient.get()
                .uri(uri -> uri.path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 10)
                        .queryParam("language", "ko")
                        .queryParam("countryCode", countryCode)
                        .build())
                .retrieve().body(JsonNode.class);
        JsonNode results = body == null ? null : body.path("results");
        if (results == null || !results.isArray()) {
            throw new IllegalStateException("지오코딩 응답 형식이 올바르지 않습니다.");
        }
        for (JsonNode result : results) {
            if (!countryCode.equalsIgnoreCase(result.path("country_code").asText())) {
                continue;
            }
            double latitude = result.path("latitude").asDouble(Double.NaN);
            double longitude = result.path("longitude").asDouble(Double.NaN);
            if (Double.isFinite(latitude) && Double.isFinite(longitude)) {
                return Optional.of(new WeatherLocation(latitude, longitude));
            }
        }
        return Optional.empty();
    }

    public List<DailyWeather> forecast(WeatherLocation location) {
        JsonNode body = forecastClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("daily", "temperature_2m_mean,rain_sum,uv_index_max")
                        .queryParam("forecast_days", 16)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve().body(JsonNode.class);
        JsonNode daily = body == null ? null : body.path("daily");
        JsonNode dates = daily == null ? null : daily.path("time");
        JsonNode temperatures = daily == null ? null : daily.path("temperature_2m_mean");
        JsonNode rain = daily == null ? null : daily.path("rain_sum");
        JsonNode uv = daily == null ? null : daily.path("uv_index_max");
        if (!sameArrayLength(dates, temperatures, rain, uv)) {
            throw new IllegalStateException("예보 응답의 일별 데이터 형식이 올바르지 않습니다.");
        }

        List<DailyWeather> result = new ArrayList<>();
        for (int index = 0; index < dates.size(); index++) {
            JsonNode temperature = temperatures.get(index);
            JsonNode rainValue = rain.get(index);
            JsonNode uvValue = uv.get(index);
            if (temperature == null || rainValue == null || uvValue == null
                    || !temperature.isNumber() || !rainValue.isNumber() || !uvValue.isNumber()) {
                continue;
            }
            try {
                result.add(new DailyWeather(LocalDate.parse(dates.get(index).asText()), temperature.asDouble(),
                        rainValue.asDouble(), uvValue.asDouble()));
            } catch (RuntimeException invalidValue) {
                // 비정상 일별 레코드는 월평균 fallback 대상으로 남긴다.
            }
        }
        return List.copyOf(result);
    }

    private boolean sameArrayLength(JsonNode... values) {
        if (values.length == 0 || values[0] == null || !values[0].isArray()) {
            return false;
        }
        int size = values[0].size();
        for (JsonNode value : values) {
            if (value == null || !value.isArray() || value.size() != size) {
                return false;
            }
        }
        return true;
    }
}
