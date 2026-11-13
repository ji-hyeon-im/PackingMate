package com.packingmate.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TagGeneratorTest {
    private final TagGenerator tagGenerator = new TagGenerator();

    @Test
    void generatesFlightWeatherLodgingPersonalAndPowerTags() {
        RuleInput input = new RuleInput(
                8,
                Set.of("business"),
                Set.of("trekking"),
                10.0,
                true,
                Set.of("shampoo"),
                Set.of("camera"),
                Set.of("power:plug-mismatch"));

        Set<String> tags = tagGenerator.generate(input);

        assertEquals(Set.of(
                "transport:flight",
                "purpose:business",
                "activity:trekking",
                "temp:cold",
                "weather:rain",
                "lodging:shampoo-provided",
                "personal:camera",
                "power:plug-mismatch"), tags);
    }

    @Test
    void usesSpecifiedTemperatureBoundariesAndOmitsUnknownTemperature() {
        assertTrue(tagGenerator.generate(input(7, 10.0)).contains("temp:cold"));
        assertTrue(tagGenerator.generate(input(7, 10.1)).contains("temp:mild"));
        assertTrue(tagGenerator.generate(input(7, 24.9)).contains("temp:mild"));
        assertTrue(tagGenerator.generate(input(7, 25.0)).contains("temp:hot"));
        assertFalse(tagGenerator.generate(input(7, null)).contains("temp:cold"));
    }

    private RuleInput input(int tripDays, Double averageTemperature) {
        return new RuleInput(tripDays, Set.of(), Set.of(), averageTemperature,
                false, Set.of(), Set.of(), Set.of());
    }
}
