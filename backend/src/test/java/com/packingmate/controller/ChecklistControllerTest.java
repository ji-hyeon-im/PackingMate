package com.packingmate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import com.packingmate.dto.ChecklistRequest;
import com.packingmate.dto.ChecklistResponse;
import com.packingmate.service.ChecklistService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChecklistController.class)
class ChecklistControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChecklistService checklistService;

    @Test
    void returnsChecklistResponse() throws Exception {
        when(checklistService.generate(any(ChecklistRequest.class))).thenReturn(
                new ChecklistResponse("JP", "Tokyo", 7, 2,
                        List.of(new ChecklistResponse.Item("passport", "Passport", Set.of(),
                                ItemCategory.DOCUMENTS_FINANCE, ItemScope.PERSONAL, 1,
                                Set.of("transport:flight"), "Required for travel."))));

        mockMvc.perform(post("/api/v1/checklists")
                        .contentType("application/json")
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripDays").value(7))
                .andExpect(jsonPath("$.items[0].itemId").value("passport"))
                .andExpect(jsonPath("$.items[0].matchedTags[0]").value("transport:flight"));
    }

    @Test
    void rejectsInvalidTravelerCountBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/checklists")
                        .contentType("application/json")
                        .content(validRequest().replace("\"travelers\": 2", "\"travelers\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("travelers"));
    }

    @Test
    void rejectsMalformedDateAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/checklists")
                        .contentType("application/json")
                        .content(validRequest().replace("2026-12-01", "01-12-2026")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    private String validRequest() {
        return """
                {
                  "countryCode": "JP",
                  "city": "Tokyo",
                  "departureDate": "2026-12-01",
                  "returnDate": "2026-12-07",
                  "travelers": 2,
                  "mode": "SIMPLE",
                  "purposes": ["leisure"],
                  "activities": [],
                  "providedLodgingAmenities": [],
                  "personalSituations": []
                }
                """;
    }
}
