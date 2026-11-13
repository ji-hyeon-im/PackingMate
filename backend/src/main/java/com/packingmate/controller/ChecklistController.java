package com.packingmate.controller;

import com.packingmate.dto.ChecklistRequest;
import com.packingmate.dto.ChecklistResponse;
import com.packingmate.service.ChecklistService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checklists")
public class ChecklistController {
    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = Objects.requireNonNull(checklistService);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ChecklistResponse generate(@Valid @RequestBody ChecklistRequest request) {
        return checklistService.generate(request);
    }
}
