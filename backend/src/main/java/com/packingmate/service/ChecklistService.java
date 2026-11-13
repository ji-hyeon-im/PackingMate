package com.packingmate.service;

import com.packingmate.domain.ChecklistItem;
import com.packingmate.dto.ChecklistRequest;
import com.packingmate.dto.ChecklistResponse;
import com.packingmate.dto.ChecklistResponse.RecommendationType;
import com.packingmate.repository.ChecklistItemRepository;
import com.packingmate.rule.ChecklistRecommendation;
import com.packingmate.rule.RuleEngine;
import com.packingmate.rule.RuleInput;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 입력 검증 후 저장된 규칙과 규칙 엔진을 연결한다. */
@Service
public class ChecklistService {
    private final ChecklistItemRepository checklistItemRepository;
    private final RuleEngine ruleEngine;
    private final CountryCatalog countryCatalog;

    public ChecklistService(ChecklistItemRepository checklistItemRepository, RuleEngine ruleEngine,
            CountryCatalog countryCatalog) {
        this.checklistItemRepository = Objects.requireNonNull(checklistItemRepository);
        this.ruleEngine = Objects.requireNonNull(ruleEngine);
        this.countryCatalog = Objects.requireNonNull(countryCatalog);
    }

    public ChecklistResponse generate(ChecklistRequest request) {
        Objects.requireNonNull(request, "요청은 필수입니다.");
        CountryCatalog.Country country = countryCatalog.findByCode(request.countryCode())
                .orElseThrow(() -> new InvalidChecklistRequestException("지원하지 않는 목적지 국가입니다."));
        if (!request.returnDate().isAfter(request.departureDate())) {
            throw new InvalidChecklistRequestException("귀국일은 출발일보다 뒤여야 합니다.");
        }
        long days = ChronoUnit.DAYS.between(request.departureDate(), request.returnDate()) + 1;
        if (days > Integer.MAX_VALUE) {
            throw new InvalidChecklistRequestException("여행 기간이 허용 범위를 초과합니다.");
        }
        int tripDays = (int) days;
        boolean standardMode = request.mode() == ChecklistRequest.ChecklistMode.STANDARD;
        RuleInput ruleInput = new RuleInput(
                tripDays,
                request.purposes(),
                request.activities(),
                null,
                false,
                standardMode ? request.providedLodgingAmenities() : Set.of(),
                standardMode ? request.personalSituations() : Set.of(),
                Set.of());
        List<ChecklistItem> items = checklistItemRepository.findAll();
        List<ChecklistResponse.Item> responseItems = ruleEngine.generate(items, ruleInput).stream()
                .map(this::toResponseItem)
                .toList();
        return new ChecklistResponse(
                country.countryCode(),
                request.city() == null || request.city().isBlank()
                        ? country.representativeCity()
                        : request.city().trim(),
                tripDays,
                request.travelers(),
                responseItems);
    }

    private ChecklistResponse.Item toResponseItem(ChecklistRecommendation recommendation) {
        ChecklistItem item = recommendation.item();
        RecommendationType recommendationType = item.requiredTags().isEmpty()
                ? RecommendationType.BASIC
                : RecommendationType.CONDITIONAL;
        return new ChecklistResponse.Item(
                item.itemId(), item.name(), item.aliases(), item.category(), item.scope(),
                recommendation.quantity(), recommendation.matchedTags(), item.reasonTemplate(), recommendationType);
    }
}
