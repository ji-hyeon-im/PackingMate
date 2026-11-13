package com.packingmate.domain;

/** 여행 일수와 항목의 상한을 이용해 준비 수량을 계산하는 규칙. */
public sealed interface QuantityRule
        permits QuantityRule.Fixed, QuantityRule.DailyCap, QuantityRule.Rotation, QuantityRule.Consumable {

    /** 지정한 기본 수량을 반환한다. */
    record Fixed(int quantity) implements QuantityRule {
        public Fixed {
            if (quantity < 1) {
                throw new IllegalArgumentException("고정 수량은 1 이상이어야 합니다.");
            }
        }
    }

    /** 매일 사용하는 항목: 여행 일수에 여분 수량을 더한다. */
    record DailyCap(int extraQuantity) implements QuantityRule {
        public DailyCap {
            if (extraQuantity < 0) {
                throw new IllegalArgumentException("여분 수량은 0 이상이어야 합니다.");
            }
        }
    }

    /** 순환 착용 항목: 일정 일수마다 한 개를 계산한다. */
    record Rotation(int daysPerUnit) implements QuantityRule {
        public Rotation {
            if (daysPerUnit < 1) {
                throw new IllegalArgumentException("수량당 일수는 1 이상이어야 합니다.");
            }
        }
    }

    /** 묶음 단위 소모품: 일정 일수마다 한 묶음을 계산한다. */
    record Consumable(int daysPerUnit) implements QuantityRule {
        public Consumable {
            if (daysPerUnit < 1) {
                throw new IllegalArgumentException("수량당 일수는 1 이상이어야 합니다.");
            }
        }
    }

    /** 여행 일수와 항목별 상한을 적용해 최종 수량을 반환한다. */
    default int calculate(int tripDays, int maxQuantity) {
        if (tripDays < 1) {
            throw new IllegalArgumentException("여행 일수는 1 이상이어야 합니다.");
        }
        if (maxQuantity < 1) {
            throw new IllegalArgumentException("최대 수량은 1 이상이어야 합니다.");
        }

        int quantity = switch (this) {
            case Fixed fixed -> fixed.quantity();
            case DailyCap dailyCap -> Math.addExact(tripDays, dailyCap.extraQuantity());
            case Rotation rotation -> divideRoundUp(tripDays, rotation.daysPerUnit());
            case Consumable consumable -> divideRoundUp(tripDays, consumable.daysPerUnit());
        };
        return Math.min(quantity, maxQuantity);
    }

    private static int divideRoundUp(int dividend, int divisor) {
        return (int) (((long) dividend + divisor - 1) / divisor);
    }
}
