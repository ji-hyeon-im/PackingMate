package com.packingmate.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.packingmate.domain.ChecklistItem;
import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import com.packingmate.domain.QuantityRule;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleEngineTest {
    private final RuleEngine ruleEngine = new RuleEngine(new TagGenerator(), new QuantityCalculator());

    @Test
    void includesUnconditionalAndOrMatchedItemsButExcludesBlockedItems() {
        ChecklistItem always = item("passport", Set.of(), Set.of(), ItemScope.PERSONAL,
                new QuantityRule.Fixed(1), 1);
        ChecklistItem conditional = item("cold-or-trekking", Set.of("temp:cold", "activity:trekking"),
                Set.of(), ItemScope.PERSONAL, new QuantityRule.Fixed(1), 1);
        ChecklistItem blocked = item("swimwear", Set.of(), Set.of("activity:no-water-activities"),
                ItemScope.PERSONAL, new QuantityRule.Fixed(1), 1);

        List<ChecklistRecommendation> result = ruleEngine.generate(
                List.of(always, conditional, blocked), Set.of("activity:trekking", "activity:no-water-activities"), 5);

        assertEquals(Set.of("passport", "cold-or-trekking"), ids(result));
        assertEquals(Set.of("activity:trekking"), result.stream()
                .filter(value -> value.item().itemId().equals("cold-or-trekking"))
                .findFirst().orElseThrow().matchedTags());
    }

    @Test
    void mergesSameItemIdAndKeepsAllMatchedReasonTags() {
        ChecklistItem businessRule = item("adapter", Set.of("purpose:business"), Set.of(),
                ItemScope.PERSONAL, new QuantityRule.Fixed(1), 1);
        ChecklistItem weatherRule = item("adapter", Set.of("weather:rain"), Set.of(),
                ItemScope.PERSONAL, new QuantityRule.Fixed(1), 1);

        List<ChecklistRecommendation> result = ruleEngine.generate(
                List.of(businessRule, weatherRule), Set.of("purpose:business", "weather:rain"), 4);

        assertEquals(1, result.size());
        assertEquals(Set.of("purpose:business", "weather:rain"), result.getFirst().matchedTags());
    }

    @Test
    void sharedItemsStayAtOneAndPersonalQuantityUsesCap() {
        ChecklistItem shared = item("shared-wipes", Set.of(), Set.of(), ItemScope.SHARED,
                new QuantityRule.DailyCap(1), 14);
        ChecklistItem personal = item("underwear", Set.of(), Set.of(), ItemScope.PERSONAL,
                new QuantityRule.DailyCap(1), 5);

        List<ChecklistRecommendation> result = ruleEngine.generate(List.of(shared, personal), Set.of(), 8);

        assertEquals(1, quantity(result, "shared-wipes"));
        assertEquals(5, quantity(result, "underwear"));
    }

    private ChecklistItem item(
            String id,
            Set<String> requiredTags,
            Set<String> excludeTags,
            ItemScope scope,
            QuantityRule quantityRule,
            int maxQuantity) {
        return new ChecklistItem(id, id, Set.of(), ItemCategory.SAFETY_OTHER, scope,
                requiredTags, excludeTags, quantityRule, maxQuantity, "reason");
    }

    private Set<String> ids(List<ChecklistRecommendation> recommendations) {
        return recommendations.stream().map(value -> value.item().itemId()).collect(java.util.stream.Collectors.toSet());
    }

    private int quantity(List<ChecklistRecommendation> recommendations, String itemId) {
        return recommendations.stream()
                .filter(value -> value.item().itemId().equals(itemId))
                .findFirst().orElseThrow().quantity();
    }
}
