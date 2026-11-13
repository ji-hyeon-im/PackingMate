package com.packingmate.domain.power;

import java.time.LocalDate;
import java.util.List;

/** 국가별 공칭 전원과 여행자가 접할 수 있는 대표 플러그 정보. */
public record PowerStandard(String countryCode, List<Supply> supplies, List<Plug> plugs) {
    public PowerStandard {
        supplies = List.copyOf(supplies);
        plugs = List.copyOf(plugs);
    }

    public record Supply(int voltage, int frequency, String note, String sourceUrl, LocalDate checkedOn) {
    }

    public record Plug(String type, String sourceUrl, LocalDate checkedOn) {
    }
}
