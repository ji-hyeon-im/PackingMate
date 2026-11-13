package com.packingmate.rule;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 여행 입력을 데이터베이스 규칙에서 사용하는 상황 태그로 변환한다. */
@Component
public class TagGenerator {
    public Set<String> generate(RuleInput input) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("transport:flight");

        input.purposes().forEach(value -> tags.add("purpose:" + value));
        input.activities().forEach(value -> tags.add("activity:" + value));
        if (input.averageTemperatureCelsius() != null) {
            tags.add(temperatureTag(input.averageTemperatureCelsius()));
        }
        if (input.rainy()) {
            tags.add("weather:rain");
        }
        input.providedLodgingAmenities().forEach(value -> tags.add("lodging:" + value + "-provided"));
        input.personalSituations().forEach(value -> tags.add("personal:" + value));
        tags.addAll(input.additionalTags());
        return Set.copyOf(tags);
    }

    private String temperatureTag(double temperatureCelsius) {
        if (temperatureCelsius <= 10.0) {
            return "temp:cold";
        }
        if (temperatureCelsius >= 25.0) {
            return "temp:hot";
        }
        return "temp:mild";
    }
}
