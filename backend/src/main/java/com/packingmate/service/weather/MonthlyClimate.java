package com.packingmate.service.weather;

import java.util.Map;

public record MonthlyClimate(Map<Integer, ClimateMonth> months, String period) {
    public MonthlyClimate {
        months = Map.copyOf(months);
        if (months.size() != 12 || period == null || period.isBlank()) {
            throw new IllegalArgumentException("월별 기후 자료가 완전하지 않습니다.");
        }
    }

    public record ClimateMonth(double meanTemperatureCelsius, double precipitationMillimetersPerDay,
            double meanUvIndex) {
        public ClimateMonth {
            if (!Double.isFinite(meanTemperatureCelsius)
                    || !Double.isFinite(precipitationMillimetersPerDay)
                    || precipitationMillimetersPerDay < 0
                    || !Double.isFinite(meanUvIndex) || meanUvIndex < 0) {
                throw new IllegalArgumentException("월별 기후 데이터 값이 올바르지 않습니다.");
            }
        }
    }
}
