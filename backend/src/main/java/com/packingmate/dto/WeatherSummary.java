package com.packingmate.dto;

/** 여행 기간에 규칙 엔진이 실제 사용한 날씨 자료의 범위와 출처. */
public record WeatherSummary(
        Source source,
        Double averageTemperatureCelsius,
        boolean rainy,
        boolean uvProtectionRecommended,
        int forecastDays,
        int monthlyAverageDays,
        String climatologyPeriod
) {
    public WeatherSummary {
        if (source == null || forecastDays < 0 || monthlyAverageDays < 0
                || (averageTemperatureCelsius != null && !Double.isFinite(averageTemperatureCelsius))) {
            throw new IllegalArgumentException("날씨 요약 값이 올바르지 않습니다.");
        }
        if (source == Source.UNAVAILABLE && averageTemperatureCelsius != null) {
            throw new IllegalArgumentException("날씨를 사용할 수 없는 경우 평균 기온은 없어야 합니다.");
        }
    }

    public static WeatherSummary unavailable() {
        return new WeatherSummary(Source.UNAVAILABLE, null, false, false, 0, 0, null);
    }

    public enum Source {
        OPEN_METEO,
        NASA_POWER,
        MIXED,
        PARTIAL_OPEN_METEO,
        UNAVAILABLE
    }
}
