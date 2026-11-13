package com.packingmate.rule;

import com.packingmate.domain.ChecklistItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 필요 태그 OR 매칭, 제외 태그 적용, 중복 병합 및 수량 계산을 수행한다. */
@Component
public class RuleEngine {
    private final TagGenerator tagGenerator;
    private final QuantityCalculator quantityCalculator;

    public RuleEngine(TagGenerator tagGenerator, QuantityCalculator quantityCalculator) {
        this.tagGenerator = Objects.requireNonNull(tagGenerator);
        this.quantityCalculator = Objects.requireNonNull(quantityCalculator);
    }

    public List<ChecklistRecommendation> generate(List<ChecklistItem> items, RuleInput input) {
        Objects.requireNonNull(input, "input은 필수입니다.");
        return generate(items, tagGenerator.generate(input), input.tripDays());
    }

    public List<ChecklistRecommendation> generate(List<ChecklistItem> items, Set<String> contextTags, int tripDays) {
        Objects.requireNonNull(items, "items는 null일 수 없습니다.");
        if (tripDays < 1) {
            throw new IllegalArgumentException("여행 일수는 1 이상이어야 합니다.");
        }
        Set<String> tags = Set.copyOf(Objects.requireNonNull(contextTags, "contextTags는 null일 수 없습니다."));
        Map<String, ChecklistRecommendation> merged = new LinkedHashMap<>();

        for (ChecklistItem item : items) {
            Objects.requireNonNull(item, "items에 null 항목이 포함되어 있습니다.");
            Set<String> matchedRequiredTags = matchingRequiredTags(item.requiredTags(), tags);
            if (!matches(item, tags, matchedRequiredTags)) {
                continue;
            }

            ChecklistRecommendation recommendation = new ChecklistRecommendation(
                    item,
                    quantityCalculator.calculate(item, tripDays),
                    matchedRequiredTags);
            merged.merge(item.itemId(), recommendation, this::mergeDuplicate);
        }

        List<ChecklistRecommendation> result = new ArrayList<>(merged.values());
        result.sort(Comparator
                .comparing((ChecklistRecommendation value) -> value.item().category())
                .thenComparing(value -> value.item().itemId()));
        return List.copyOf(result);
    }

    private boolean matches(ChecklistItem item, Set<String> contextTags, Set<String> matchedRequiredTags) {
        boolean excluded = item.excludeTags().stream().anyMatch(contextTags::contains);
        if (excluded) {
            return false;
        }
        return item.requiredTags().isEmpty() || !matchedRequiredTags.isEmpty();
    }

    private Set<String> matchingRequiredTags(Set<String> requiredTags, Set<String> contextTags) {
        LinkedHashSet<String> matches = new LinkedHashSet<>(requiredTags);
        matches.retainAll(contextTags);
        return Set.copyOf(matches);
    }

    private ChecklistRecommendation mergeDuplicate(
            ChecklistRecommendation existing,
            ChecklistRecommendation duplicate) {
        LinkedHashSet<String> combinedTags = new LinkedHashSet<>(existing.matchedTags());
        combinedTags.addAll(duplicate.matchedTags());
        return new ChecklistRecommendation(
                existing.item(),
                Math.max(existing.quantity(), duplicate.quantity()),
                combinedTags);
    }
}
