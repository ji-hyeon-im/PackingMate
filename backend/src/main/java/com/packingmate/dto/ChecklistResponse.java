package com.packingmate.dto;

import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import java.util.List;
import java.util.Set;

/** 규칙 엔진 결과를 전달하는 API 출력 계약. */
public record ChecklistResponse(
        String countryCode,
        String city,
        int tripDays,
        int travelers,
        List<Item> items
) {
    public ChecklistResponse {
        items = List.copyOf(items);
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
