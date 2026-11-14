package com.packingmate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

/** 체크리스트 생성을 위한 API 입력 계약. */
public record ChecklistRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z]{2}", message = "목적지 국가는 ISO 3166-1 alpha-2 코드여야 합니다.")
        String countryCode,
        @Size(max = 100) String city,
        @NotNull LocalDate departureDate,
        @NotNull LocalDate returnDate,
        @NotNull @Min(1) Integer travelers,
        @NotNull ChecklistMode mode,
        @NotNull Set<@NotBlank @Size(max = 60) @Pattern(regexp = "(?:leisure|business)", message = "지원하는 여행 목적을 선택해야 합니다.") String> purposes,
        @NotNull Set<@NotBlank @Size(max = 60) @Pattern(regexp = "(?:trekking|swimming|ski)", message = "지원하는 활동을 선택해야 합니다.") String> activities,
        @NotNull Set<@NotBlank @Size(max = 60) @Pattern(regexp = "[a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*") String> providedLodgingAmenities,
        @NotNull Set<@NotBlank @Size(max = 60) @Pattern(regexp = "[a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*") String> personalSituations
) {
    public enum ChecklistMode {
        SIMPLE,
        STANDARD
    }
}
