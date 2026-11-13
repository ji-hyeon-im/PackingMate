package com.packingmate.rule;

import com.packingmate.domain.ChecklistItem;
import java.util.Objects;
import java.util.Set;

/** 규칙 엔진이 체크리스트에 반환하는 항목과 추천 근거 태그. */
public record ChecklistRecommendation(ChecklistItem item, int quantity, Set<String> matchedTags) {
    public ChecklistRecommendation {
        Objects.requireNonNull(item, "item은 필수입니다.");
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        matchedTags = Set.copyOf(Objects.requireNonNull(matchedTags, "matchedTags는 null일 수 없습니다."));
    }
}
