package com.packingmate.dto;

import java.time.LocalDate;
import java.util.List;

/** 목적지 전원 정보와 한국 플러그 기준 비교 결과. */
public record PowerSummary(
        List<Supply> supplies,
        List<String> plugTypes,
        PlugCompatibility plugCompatibility,
        boolean voltageCheckRecommended,
        boolean frequencyCheckRecommended,
        List<Source> sources
) {
    public PowerSummary {
        supplies = List.copyOf(supplies);
        plugTypes = List.copyOf(plugTypes);
        sources = List.copyOf(sources);
    }

    public static PowerSummary unavailable() {
        return new PowerSummary(List.of(), List.of(), PlugCompatibility.COMPATIBLE, false, false, List.of());
    }

    public enum PlugCompatibility {
        COMPATIBLE,
        PARTIAL,
        INCOMPATIBLE
    }

    public record Supply(int voltage, int frequency, String note) {
    }

    public record Source(String kind, String url, LocalDate checkedOn) {
    }
}
