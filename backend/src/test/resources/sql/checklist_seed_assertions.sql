-- schema.sql과 data.sql을 적용한 뒤 psql에서 실행하는 시드 데이터 검증.
DO $$
DECLARE
    item_count INTEGER;
    alias_count INTEGER;
    required_tag_count INTEGER;
BEGIN
    SELECT count(*) INTO item_count FROM checklist_item;
    IF item_count < 40 OR item_count > 60 THEN
        RAISE EXCEPTION '준비물 시드 수가 40~60개 범위를 벗어났습니다: %', item_count;
    END IF;

    SELECT count(*) INTO alias_count FROM checklist_item_alias;
    IF alias_count < 10 THEN
        RAISE EXCEPTION '별칭 시드 수가 너무 적습니다: %', alias_count;
    END IF;

    SELECT count(*) INTO required_tag_count FROM checklist_item_required_tag;
    IF required_tag_count < 10 THEN
        RAISE EXCEPTION '조건 태그 시드 수가 너무 적습니다: %', required_tag_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM checklist_item
        WHERE item_id = 'underwear'
          AND quantity_rule_type = 'DAILY_CAP'
          AND quantity_rule_parameter = 1
          AND max_quantity = 14
    ) THEN
        RAISE EXCEPTION '속옷 DAILY_CAP 규칙 데이터가 기대값과 다릅니다.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM checklist_item
        WHERE item_id = 'tops'
          AND quantity_rule_type = 'ROTATION'
          AND quantity_rule_parameter = 2
    ) THEN
        RAISE EXCEPTION '상의 ROTATION 규칙 데이터가 기대값과 다릅니다.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM checklist_item
        WHERE item_id = 'wet-wipes'
          AND scope = 'PERSONAL'
          AND quantity_rule_type = 'CONSUMABLE'
    ) THEN
        RAISE EXCEPTION '개인용 소모품 규칙 데이터가 기대값과 다릅니다.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM checklist_item
        WHERE scope = 'SHARED'
          AND (quantity_rule_type <> 'FIXED' OR quantity_rule_parameter <> 1 OR max_quantity <> 1)
    ) THEN
        RAISE EXCEPTION '공동용 항목은 수량 1 고정이어야 합니다.';
    END IF;

    RAISE NOTICE '체크리스트 시드 검증 성공: 항목 %, 별칭 %, 조건 태그 %',
        item_count, alias_count, required_tag_count;
END $$;
