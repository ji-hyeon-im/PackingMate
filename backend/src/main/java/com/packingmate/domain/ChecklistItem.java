package com.packingmate.domain;

import java.util.Objects;
import java.util.Set;

/** 태그 규칙 엔진이 사용하는 기본 준비물 정의. */
public record ChecklistItem(
        String itemId,
        String name,
        Set<String> aliases,
        ItemCategory category,
        ItemScope scope,
        Set<String> requiredTags,
        Set<String> excludeTags,
        QuantityRule quantityRule,
        int maxQuantity,
        String reasonTemplate
) {
    public ChecklistItem {
        itemId = requireText(itemId, "itemId");
        name = requireText(name, "name");
        aliases = immutableSet(aliases, "aliases");
        category = Objects.requireNonNull(category, "category는 필수입니다.");
        scope = Objects.requireNonNull(scope, "scope는 필수입니다.");
        requiredTags = immutableSet(requiredTags, "requiredTags");
        excludeTags = immutableSet(excludeTags, "excludeTags");
        quantityRule = Objects.requireNonNull(quantityRule, "quantityRule은 필수입니다.");
        if (maxQuantity < 1) {
            throw new IllegalArgumentException("maxQuantity는 1 이상이어야 합니다.");
        }
        reasonTemplate = reasonTemplate == null ? "" : reasonTemplate.trim();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }
        return value.trim();
    }

    private static Set<String> immutableSet(Set<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + "은(는) null일 수 없습니다.");
        return Set.copyOf(values);
    }
}
