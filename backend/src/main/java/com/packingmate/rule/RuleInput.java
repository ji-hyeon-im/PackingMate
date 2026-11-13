package com.packingmate.rule;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** 규칙 엔진에 전달하는 여행 조건. 토큰은 영문 소문자와 하이픈으로 전달한다. */
public record RuleInput(
        int tripDays,
        Set<String> purposes,
        Set<String> activities,
        Double averageTemperatureCelsius,
        boolean rainy,
        Set<String> providedLodgingAmenities,
        Set<String> personalSituations,
        Set<String> additionalTags
) {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private static final Pattern TAG_PATTERN = Pattern.compile("[a-z][a-z0-9-]*:[a-z0-9]+(?:-[a-z0-9]+)*");

    public RuleInput {
        if (tripDays < 1) {
            throw new IllegalArgumentException("여행 일수는 1 이상이어야 합니다.");
        }
        purposes = normalizeTokens(purposes, "purposes");
        activities = normalizeTokens(activities, "activities");
        providedLodgingAmenities = normalizeTokens(providedLodgingAmenities, "providedLodgingAmenities");
        personalSituations = normalizeTokens(personalSituations, "personalSituations");
        additionalTags = normalizeTags(additionalTags);
        if (averageTemperatureCelsius != null && !Double.isFinite(averageTemperatureCelsius)) {
            throw new IllegalArgumentException("평균 기온은 유한한 숫자여야 합니다.");
        }
    }

    private static Set<String> normalizeTokens(Set<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + "은(는) null일 수 없습니다.");
        return values.stream()
                .map(value -> normalizeToken(value, fieldName))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalizeToken(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 항목은 비어 있을 수 없습니다.");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!TOKEN_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " 항목은 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다: " + value);
        }
        return normalized;
    }

    private static Set<String> normalizeTags(Set<String> values) {
        Objects.requireNonNull(values, "additionalTags는 null일 수 없습니다.");
        return values.stream()
                .map(value -> {
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("additionalTags 항목은 비어 있을 수 없습니다.");
                    }
                    String normalized = value.trim().toLowerCase(Locale.ROOT);
                    if (!TAG_PATTERN.matcher(normalized).matches()) {
                        throw new IllegalArgumentException("추가 태그 형식이 올바르지 않습니다: " + value);
                    }
                    return normalized;
                })
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
