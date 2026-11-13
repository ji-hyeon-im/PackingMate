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
        return new ChecklistService(repository, new RuleEngine(new TagGenerator(), new QuantityCalculator()));
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
        verify(repository).findAll();
    }

    @Test
    void rejectsReturnDateThatIsNotAfterDepartureDate() {
        assertThrows(InvalidChecklistRequestException.class,
                () -> service().generate(request("2026-12-07", "2026-12-07")));
    }

    private ChecklistRequest request(String departure, String returning) {
        return new ChecklistRequest("JP", "Tokyo", LocalDate.parse(departure), LocalDate.parse(returning),
                2, ChecklistRequest.ChecklistMode.SIMPLE,
                Set.of("leisure"), Set.of(), Set.of(), Set.of());
    }
}
