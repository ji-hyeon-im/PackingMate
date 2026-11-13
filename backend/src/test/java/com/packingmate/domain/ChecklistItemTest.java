package com.packingmate.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ChecklistItemTest {

    @Test
    void dailyCapAddsExtraQuantityAndAppliesMaximum() {
        QuantityRule rule = new QuantityRule.DailyCap(1);

        assertEquals(4, rule.calculate(3, 10));
        assertEquals(5, rule.calculate(8, 5));
    }

    @Test
    void rotationAndConsumableRoundUpToWholeUnits() {
        assertEquals(3, new QuantityRule.Rotation(2).calculate(5, 10));
        assertEquals(2, new QuantityRule.Consumable(3).calculate(4, 10));
    }

    @Test
    void fixedRuleReturnsConfiguredQuantityWithinMaximum() {
        assertEquals(2, new QuantityRule.Fixed(2).calculate(7, 5));
        assertEquals(3, new QuantityRule.Fixed(4).calculate(7, 3));
    }

    @Test
    void checklistItemCopiesTagCollectionsAndRequiresValidMaximum() {
        Set<String> tags = new java.util.HashSet<>(Set.of("activity:trekking"));
        ChecklistItem item = new ChecklistItem(
                "trekking-poles", "등산 스틱", Set.of(), ItemCategory.SAFETY_OTHER,
                ItemScope.PERSONAL, tags, Set.of(), new QuantityRule.Fixed(1), 1, "등산 활동용");
        tags.add("weather:rain");

        assertEquals(Set.of("activity:trekking"), item.requiredTags());
        assertThrows(IllegalArgumentException.class, () -> new ChecklistItem(
                "passport", "여권", Set.of(), ItemCategory.DOCUMENTS_FINANCE,
                ItemScope.PERSONAL, Set.of(), Set.of(), new QuantityRule.Fixed(1), 0, ""));
    }

    @Test
    void rejectsInvalidTripDaysAndRuleParameters() {
        assertThrows(IllegalArgumentException.class, () -> new QuantityRule.Rotation(0));
        assertThrows(IllegalArgumentException.class, () -> new QuantityRule.DailyCap(-1));
        assertThrows(IllegalArgumentException.class, () -> new QuantityRule.Fixed(1).calculate(0, 5));
    }
}
