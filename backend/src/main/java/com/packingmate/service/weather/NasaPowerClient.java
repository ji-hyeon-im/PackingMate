package com.packingmate.service.weather;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** NASA POWER의 월별 climatology API를 호출하고 응답 경계에서 값을 검증한다. */
@Component
public class NasaPowerClient {
    private static final double FILL_VALUE = -999.0;
    private final RestClient client = WeatherRestClientFactory.create("https://power.larc.nasa.gov");

    public MonthlyClimate climatology(WeatherLocation location) {
        JsonNode body = client.get()
                .uri(uri -> uri.path("/api/temporal/climatology/point")
                        .queryParam("parameters", "T2M,PRECTOTCORR,ALLSKY_SFC_UV_INDEX")
                        .queryParam("community", "AG")
                        .queryParam("longitude", location.longitude())
                        .queryParam("latitude", location.latitude())
                        .queryParam("format", "JSON")
                        .build())
                .retrieve().body(JsonNode.class);
        JsonNode properties = body == null ? null : body.path("properties");
        JsonNode parameters = properties == null ? null : properties.path("parameter");
        JsonNode temperature = parameters == null ? null : parameters.path("T2M");
        JsonNode precipitation = parameters == null ? null : parameters.path("PRECTOTCORR");
        JsonNode uvIndex = parameters == null ? null : parameters.path("ALLSKY_SFC_UV_INDEX");
        String period = body == null ? "" : body.path("header").path("range").asText("");
        if (temperature == null || precipitation == null || uvIndex == null || period.isBlank()) {
            throw new IllegalStateException("NASA POWER 월평년 응답 형식이 올바르지 않습니다.");
        }

        Map<Integer, MonthlyClimate.ClimateMonth> months = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            String key = java.time.Month.of(month).name().substring(0, 3);
            double meanTemperature = validValue(temperature.path(key));
            double precipitationPerDay = validValue(precipitation.path(key));
            double meanUvIndex = validValue(uvIndex.path(key));
            months.put(month, new MonthlyClimate.ClimateMonth(meanTemperature,
                    precipitationPerDay, meanUvIndex));
        }
        return new MonthlyClimate(months, period);
    }

    private double validValue(JsonNode value) {
        double parsed = value == null ? Double.NaN : value.asDouble(Double.NaN);
        if (!Double.isFinite(parsed) || parsed == FILL_VALUE) {
            throw new IllegalStateException("NASA POWER 월평년 값이 누락되었거나 유효하지 않습니다.");
        }
        return parsed;
    }
}
