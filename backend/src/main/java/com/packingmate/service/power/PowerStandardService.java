package com.packingmate.service.power;

import com.packingmate.domain.power.PowerStandard;
import com.packingmate.dto.PowerSummary;
import com.packingmate.repository.PowerStandardRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 한국 출발 기준으로 플러그와 전압 주의 정보를 계산한다. */
@Service
public class PowerStandardService {
    private static final int HOME_VOLTAGE = 220;
    private static final int HOME_FREQUENCY = 60;
    private static final Set<String> HOME_PLUG_TYPES = Set.of("C", "F");

    private final PowerStandardRepository repository;

    public PowerStandardService(PowerStandardRepository repository) {
        this.repository = repository;
    }

    public PowerSummary summarize(String countryCode) {
        PowerStandard standard = repository.findByCountryCode(countryCode)
                .orElseThrow(() -> new IllegalStateException("전압·플러그 기준 자료가 없습니다: " + countryCode));
        Set<String> destinationPlugTypes = standard.plugs().stream()
                .map(PowerStandard.Plug::type)
                .collect(java.util.stream.Collectors.toSet());
        boolean plugMismatch = !destinationPlugTypes.containsAll(HOME_PLUG_TYPES);
        boolean voltageCheckRecommended = standard.supplies().stream()
                .anyMatch(supply -> supply.voltage() != HOME_VOLTAGE);
        boolean frequencyCheckRecommended = standard.supplies().stream()
                .anyMatch(supply -> supply.frequency() != HOME_FREQUENCY);
        List<PowerSummary.Supply> supplies = standard.supplies().stream()
                .map(supply -> new PowerSummary.Supply(supply.voltage(), supply.frequency(), supply.note()))
                .toList();
        List<String> plugTypes = standard.plugs().stream().map(PowerStandard.Plug::type).distinct().sorted().toList();
        List<PowerSummary.Source> sources = new ArrayList<>();
        standard.supplies().stream().map(PowerStandard.Supply::sourceUrl).distinct().forEach(url ->
                sources.add(new PowerSummary.Source("SUPPLY", url,
                        latestSupplyCheck(standard, url))));
        standard.plugs().stream().map(PowerStandard.Plug::sourceUrl).distinct().forEach(url ->
                sources.add(new PowerSummary.Source("PLUG", url,
                        latestPlugCheck(standard, url))));
        return new PowerSummary(supplies, plugTypes, plugMismatch, voltageCheckRecommended,
                frequencyCheckRecommended, sources);
    }

    private LocalDate latestSupplyCheck(PowerStandard standard, String url) {
        return standard.supplies().stream().filter(row -> row.sourceUrl().equals(url))
                .map(PowerStandard.Supply::checkedOn).max(LocalDate::compareTo).orElseThrow();
    }

    private LocalDate latestPlugCheck(PowerStandard standard, String url) {
        return standard.plugs().stream().filter(row -> row.sourceUrl().equals(url))
                .map(PowerStandard.Plug::checkedOn).max(LocalDate::compareTo).orElseThrow();
    }
}
