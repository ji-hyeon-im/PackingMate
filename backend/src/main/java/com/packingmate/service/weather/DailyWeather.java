package com.packingmate.service.weather;

import java.time.LocalDate;

public record DailyWeather(LocalDate date, double meanTemperatureCelsius, double rainMillimeters,
        double maximumUvIndex) {
    public DailyWeather {
        if (date == null || !Double.isFinite(meanTemperatureCelsius)
                || !Double.isFinite(rainMillimeters) || rainMillimeters < 0
                || !Double.isFinite(maximumUvIndex) || maximumUvIndex < 0) {
            throw new IllegalArgumentException("날씨 데이터 값이 올바르지 않습니다.");
        }
    }
}
