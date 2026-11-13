package com.packingmate.service.weather;

import com.packingmate.dto.WeatherSummary;
import com.packingmate.service.WeatherLocationNotFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** 예보와 월별 기후평년값을 결합하고, 여행 기간의 요약 태그를 만든다. */
@Service
public class WeatherService {
    private static final double RAINY_DAY_THRESHOLD_MM = 1.0;
    private static final double UV_PROTECTION_THRESHOLD = 3.0;

    private final OpenMeteoClient openMeteoClient;
    private final NasaPowerClient nasaPowerClient;

    public WeatherService(OpenMeteoClient openMeteoClient, NasaPowerClient nasaPowerClient) {
        this.openMeteoClient = openMeteoClient;
        this.nasaPowerClient = nasaPowerClient;
    }

    public WeatherSummary summarize(String countryCode, String city, LocalDate departure, LocalDate returning) {
        Optional<WeatherLocation> location;
        try {
            location = openMeteoClient.locate(city, countryCode);
        } catch (RuntimeException externalFailure) {
            return WeatherSummary.unavailable();
        }
        if (location.isEmpty()) {
            throw new WeatherLocationNotFoundException();
        }

        Map<LocalDate, DailyWeather> forecastByDate = new HashMap<>();
        try {
            for (DailyWeather day : openMeteoClient.forecast(location.get())) {
                if (!day.date().isBefore(departure) && !day.date().isAfter(returning)) {
                    forecastByDate.put(day.date(), day);
                }
            }
        } catch (RuntimeException externalFailure) {
            forecastByDate.clear();
        }

        Map<Integer, Long> fallbackDaysByMonth = countDaysByMonth(departure, returning);
        forecastByDate.keySet().forEach(date -> fallbackDaysByMonth.compute(date.getMonthValue(),
                (month, count) -> count - 1));
        long monthlyDays = fallbackDaysByMonth.values().stream().mapToLong(Long::longValue).sum();

        MonthlyClimate climate = null;
        if (monthlyDays > 0) {
            try {
                climate = nasaPowerClient.climatology(location.get());
            } catch (RuntimeException externalFailure) {
                // 사용 가능한 예보가 있으면 해당 날짜만 표시하고, 나머지는 적용하지 않는다.
            }
        }

        if (climate == null && monthlyDays > 0) {
            if (forecastByDate.isEmpty()) {
                return WeatherSummary.unavailable();
            }
            return summarizeForecastOnly(forecastByDate);
        }

        double temperatureTotal = 0;
        boolean rainy = false;
        boolean uvProtection = false;
        for (DailyWeather day : forecastByDate.values()) {
            temperatureTotal += day.meanTemperatureCelsius();
            rainy |= day.rainMillimeters() >= RAINY_DAY_THRESHOLD_MM;
            uvProtection |= day.maximumUvIndex() >= UV_PROTECTION_THRESHOLD;
        }
        if (climate != null) {
            for (Map.Entry<Integer, Long> entry : fallbackDaysByMonth.entrySet()) {
                long days = entry.getValue();
                if (days <= 0) {
                    continue;
                }
                MonthlyClimate.ClimateMonth month = climate.months().get(entry.getKey());
                temperatureTotal += month.meanTemperatureCelsius() * days;
                rainy |= month.precipitationMillimetersPerDay() >= RAINY_DAY_THRESHOLD_MM;
                uvProtection |= month.meanUvIndex() >= UV_PROTECTION_THRESHOLD;
            }
        }
        long coveredDays = forecastByDate.size() + (climate == null ? 0 : monthlyDays);
        if (coveredDays == 0) {
            return WeatherSummary.unavailable();
        }
        WeatherSummary.Source source = sourceFor(forecastByDate.size(), climate == null ? 0 : monthlyDays);
        return new WeatherSummary(source, temperatureTotal / coveredDays, rainy, uvProtection,
                forecastByDate.size(), climate == null ? 0 : Math.toIntExact(monthlyDays),
                climate == null ? null : climate.period());
    }

    private WeatherSummary summarizeForecastOnly(Map<LocalDate, DailyWeather> forecastByDate) {
        double temperatureTotal = forecastByDate.values().stream()
                .mapToDouble(DailyWeather::meanTemperatureCelsius).sum();
        boolean rainy = forecastByDate.values().stream()
                .anyMatch(day -> day.rainMillimeters() >= RAINY_DAY_THRESHOLD_MM);
        boolean uvProtection = forecastByDate.values().stream()
                .anyMatch(day -> day.maximumUvIndex() >= UV_PROTECTION_THRESHOLD);
        return new WeatherSummary(WeatherSummary.Source.PARTIAL_OPEN_METEO,
                temperatureTotal / forecastByDate.size(), rainy, uvProtection,
                forecastByDate.size(), 0, null);
    }

    private WeatherSummary.Source sourceFor(int forecastDays, long monthlyDays) {
        if (forecastDays > 0 && monthlyDays > 0) {
            return WeatherSummary.Source.MIXED;
        }
        if (forecastDays > 0) {
            return WeatherSummary.Source.OPEN_METEO;
        }
        return WeatherSummary.Source.NASA_POWER;
    }

    /** 여행 기간이 매우 길어도 12개월의 날짜 수만 계산한다. */
    private Map<Integer, Long> countDaysByMonth(LocalDate start, LocalDate end) {
        Map<Integer, Long> counts = new HashMap<>();
        if (start.getYear() == end.getYear()) {
            addPartialYear(counts, start.getYear(), start.getMonthValue(), start.getDayOfMonth(),
                    end.getMonthValue(), end.getDayOfMonth());
            return counts;
        }
        addPartialYear(counts, start.getYear(), start.getMonthValue(), start.getDayOfMonth(), 12, 31);
        addPartialYear(counts, end.getYear(), 1, 1, end.getMonthValue(), end.getDayOfMonth());

        int firstFullYear = start.getYear() + 1;
        int lastFullYear = end.getYear() - 1;
        if (firstFullYear <= lastFullYear) {
            long fullYears = (long) lastFullYear - firstFullYear + 1;
            for (Month month : Month.values()) {
                long days = fullYears * month.length(false);
                if (month == Month.FEBRUARY) {
                    days += leapYears(firstFullYear, lastFullYear);
                }
                counts.merge(month.getValue(), days, Long::sum);
            }
        }
        return counts;
    }

    private void addPartialYear(Map<Integer, Long> counts, int year, int firstMonth, int firstDay,
            int lastMonth, int lastDay) {
        for (int month = firstMonth; month <= lastMonth; month++) {
            int from = month == firstMonth ? firstDay : 1;
            int through = month == lastMonth ? lastDay : Month.of(month).length(Year.isLeap(year));
            counts.merge(month, (long) through - from + 1, Long::sum);
        }
    }

    private long leapYears(int firstYear, int lastYear) {
        return divisibleBy(lastYear, 4) - divisibleBy(firstYear - 1, 4)
                - divisibleBy(lastYear, 100) + divisibleBy(firstYear - 1, 100)
                + divisibleBy(lastYear, 400) - divisibleBy(firstYear - 1, 400);
    }

    private long divisibleBy(int year, int divisor) {
        return Math.floorDiv((long) year, divisor);
    }
}
