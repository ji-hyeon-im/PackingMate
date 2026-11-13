package com.packingmate.dto;

import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import java.util.List;
import java.util.Set;

/** 규칙 엔진, 날씨, 전원 기준을 포함한 체크리스트 결과 계약. */
public record ChecklistResponse(
        String countryCode,
        String city,
        int tripDays,
        int travelers,
        List<Item> items,
        WeatherSummary weather,
        PowerSummary power
) {
    public ChecklistResponse {
        items = List.copyOf(items);
        weather = weather == null ? WeatherSummary.unavailable() : weather;
        power = power == null ? PowerSummary.unavailable() : power;
    }

    /** 날씨 응답 추가 전 호출부와의 소스 호환을 유지한다. */
    public ChecklistResponse(String countryCode, String city, int tripDays, int travelers, List<Item> items,
            WeatherSummary weather) {
        this(countryCode, city, tripDays, travelers, items, weather, PowerSummary.unavailable());
    }

    /** 기존 서비스 테스트와 내부 생성 호출의 소스 호환을 유지한다. */
    public ChecklistResponse(String countryCode, String city, int tripDays, int travelers, List<Item> items) {
        this(countryCode, city, tripDays, travelers, items, WeatherSummary.unavailable(), PowerSummary.unavailable());
    }

    public record Item(
            String itemId,
            String name,
            Set<String> aliases,
            ItemCategory category,
            ItemScope scope,
            int quantity,
            Set<String> matchedTags,
            String reasonTemplate,
            RecommendationType recommendationType
    ) {
        public Item {
            aliases = Set.copyOf(aliases);
            matchedTags = Set.copyOf(matchedTags);
        }
    }

    public enum RecommendationType {
        BASIC,
        CONDITIONAL
    }
}
