package com.packingmate.service.weather;

public record WeatherLocation(double latitude, double longitude) {
    public WeatherLocation {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("좌표 범위가 올바르지 않습니다.");
        }
    }
}
