package com.packingmate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.packingmate.domain.ChecklistItem;
import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import com.packingmate.domain.QuantityRule;
import com.packingmate.dto.ChecklistRequest;
import com.packingmate.dto.ChecklistResponse.RecommendationType;
import com.packingmate.repository.ChecklistItemRepository;
import com.packingmate.rule.QuantityCalculator;
import com.packingmate.rule.RuleEngine;
import com.packingmate.rule.TagGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {
    @Mock
    private ChecklistItemRepository repository;

    private ChecklistService service() {
        return new ChecklistService(repository, new RuleEngine(new TagGenerator(), new QuantityCalculator()),
                new CountryCatalog());
    }

    @Test
    void generatesChecklistAndCountsDepartureAndReturnDates() {
        ChecklistItem item = new ChecklistItem("passport", "Passport", Set.of("travel document"),
                ItemCategory.DOCUMENTS_FINANCE, ItemScope.PERSONAL, Set.of(), Set.of(),
                new QuantityRule.Fixed(1), 1, "Required for international travel.");
        when(repository.findAll()).thenReturn(List.of(item));

        var result = service().generate(request("2026-12-01", "2026-12-07"));

        assertEquals("JP", result.countryCode());
        assertEquals(7, result.tripDays());
        assertEquals(2, result.travelers());
        assertEquals("passport", result.items().getFirst().itemId());
        assertEquals(1, result.items().getFirst().quantity());
        assertEquals(RecommendationType.BASIC, result.items().getFirst().recommendationType());
        verify(repository).findAll();
    }

    @Test
    void rejectsReturnDateThatIsNotAfterDepartureDate() {
        assertThrows(InvalidChecklistRequestException.class,
                () -> service().generate(request("2026-12-07", "2026-12-07")));
    }

    @Test
    void rejectsCountryOutsideSupportedCatalog() {
        ChecklistRequest unsupported = new ChecklistRequest("FR", "Paris", LocalDate.parse("2026-12-01"),
                LocalDate.parse("2026-12-02"), 1, ChecklistRequest.ChecklistMode.SIMPLE,
                Set.of(), Set.of(), Set.of(), Set.of());

        assertThrows(InvalidChecklistRequestException.class, () -> service().generate(unsupported));
    }

    @Test
    void usesRepresentativeCityWhenCityIsBlank() {
        when(repository.findAll()).thenReturn(List.of());

        var result = service().generate(new ChecklistRequest("JP", " ", LocalDate.parse("2026-12-01"),
                LocalDate.parse("2026-12-02"), 1, ChecklistRequest.ChecklistMode.SIMPLE,
                Set.of(), Set.of(), Set.of(), Set.of()));

        assertEquals("도쿄", result.city());
    }

    @Test
    void simpleModeIgnoresStandardOnlyLodgingAndPersonalSituations() {
        ChecklistItem shampoo = new ChecklistItem("shampoo", "Shampoo", Set.of(),
                ItemCategory.TOILETRIES_HYGIENE, ItemScope.PERSONAL, Set.of(), Set.of("lodging:shampoo-provided"),
                new QuantityRule.Fixed(1), 1, "Bring shampoo if it is not provided.");
        ChecklistItem glasses = new ChecklistItem("glasses", "Glasses", Set.of(),
                ItemCategory.MEDICINE_HEALTH, ItemScope.PERSONAL, Set.of("personal:glasses"), Set.of(),
                new QuantityRule.Fixed(1), 1, "Bring glasses if needed.");
        when(repository.findAll()).thenReturn(List.of(shampoo, glasses));
        ChecklistRequest simpleRequest = new ChecklistRequest("JP", "Tokyo", LocalDate.parse("2026-12-01"),
                LocalDate.parse("2026-12-02"), 1, ChecklistRequest.ChecklistMode.SIMPLE,
                Set.of(), Set.of(), Set.of("shampoo"), Set.of("glasses"));

        var result = service().generate(simpleRequest);

        assertEquals(List.of("shampoo"), result.items().stream().map(item -> item.itemId()).toList());
        assertEquals(RecommendationType.BASIC, result.items().getFirst().recommendationType());
    }

    @Test
    void standardModeAppliesLodgingAndPersonalSituationInputs() {
        ChecklistItem shampoo = new ChecklistItem("shampoo", "Shampoo", Set.of(),
                ItemCategory.TOILETRIES_HYGIENE, ItemScope.PERSONAL, Set.of(), Set.of("lodging:shampoo-provided"),
                new QuantityRule.Fixed(1), 1, "Bring shampoo if it is not provided.");
        ChecklistItem glasses = new ChecklistItem("glasses", "Glasses", Set.of(),
                ItemCategory.MEDICINE_HEALTH, ItemScope.PERSONAL, Set.of("personal:glasses"), Set.of(),
                new QuantityRule.Fixed(1), 1, "Bring glasses if needed.");
        when(repository.findAll()).thenReturn(List.of(shampoo, glasses));
        ChecklistRequest standardRequest = new ChecklistRequest("JP", "Tokyo", LocalDate.parse("2026-12-01"),
                LocalDate.parse("2026-12-02"), 1, ChecklistRequest.ChecklistMode.STANDARD,
                Set.of(), Set.of(), Set.of("shampoo"), Set.of("glasses"));

        var result = service().generate(standardRequest);

        assertEquals(List.of("glasses"), result.items().stream().map(item -> item.itemId()).toList());
        assertEquals(RecommendationType.CONDITIONAL, result.items().getFirst().recommendationType());
    }

    private ChecklistRequest request(String departure, String returning) {
        return new ChecklistRequest("JP", "Tokyo", LocalDate.parse(departure), LocalDate.parse(returning),
                2, ChecklistRequest.ChecklistMode.SIMPLE,
                Set.of("leisure"), Set.of(), Set.of(), Set.of());
    }
}
