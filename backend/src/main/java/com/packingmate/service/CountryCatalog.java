package com.packingmate.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** MVP에서 지원하는 목적지 국가 목록. ISO 코드와 화면 표시명, 도시 미입력 대체값을 함께 관리한다. */
@Service
public class CountryCatalog {
    private static final List<Country> COUNTRIES = List.of(
            new Country("JP", "일본", "도쿄"),
            new Country("VN", "베트남", "하노이"),
            new Country("US", "미국", "워싱턴 D.C."),
            new Country("TH", "태국", "방콕"),
            new Country("TW", "대만", "타이베이"));

    public List<Country> findAll() {
        return COUNTRIES;
    }

    public Optional<Country> findByCode(String countryCode) {
        if (countryCode == null) {
            return Optional.empty();
        }
        String normalizedCode = countryCode.trim().toUpperCase(Locale.ROOT);
        return COUNTRIES.stream().filter(country -> country.countryCode().equals(normalizedCode)).findFirst();
    }

    public record Country(String countryCode, String name, String representativeCity) {
    }
}
