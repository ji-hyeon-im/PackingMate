package com.packingmate.rule;

import com.packingmate.domain.ChecklistItem;
import com.packingmate.domain.ItemScope;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 항목 범위에 맞춰 개인용 수량 또는 공동용 고정 수량을 계산한다. */
@Component
public class QuantityCalculator {
    public int calculate(ChecklistItem item, int tripDays) {
        Objects.requireNonNull(item, "item은 필수입니다.");
        if (tripDays < 1) {
            throw new IllegalArgumentException("여행 일수는 1 이상이어야 합니다.");
        }
        if (item.scope() == ItemScope.SHARED) {
            return 1;
        }
        return item.quantityRule().calculate(tripDays, item.maxQuantity());
    }
}
