-- MVP 기본 준비물 52개. 조건 태그(required_tag)는 OR 조건으로 매칭하고,
-- 비어 있는 required_tag 목록은 항상 포함되는 항목으로 취급한다.
-- 체크리스트 기준 데이터는 SQL 시드가 원본이며, 매 실행 때 아래 전체 데이터 집합으로 동기화한다.

BEGIN;

DELETE FROM country_power_plug;
DELETE FROM country_power_supply;
DELETE FROM checklist_item_alias;
DELETE FROM checklist_item_required_tag;
DELETE FROM checklist_item_exclude_tag;
DELETE FROM checklist_item;

INSERT INTO checklist_item (
    item_id, name, category, scope, quantity_rule_type,
    quantity_rule_parameter, max_quantity, reason_template
) VALUES
    ('passport', '여권', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '해외여행 중 신분과 국적을 확인할 때 필요합니다.'),
    ('passport-copy', '여권 사본', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 2, '여권 분실에 대비해 사본을 별도로 보관할 수 있습니다.'),
    ('flight-ticket', '항공권', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '항공편 탑승 정보를 확인할 때 필요합니다.'),
    ('travel-insurance', '여행자 보험 정보', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '보험 가입 정보와 긴급 연락처를 확인할 수 있습니다.'),
    ('payment-card', '해외 결제 카드', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 2, '현지 결제와 긴급 상황에 대비할 수 있습니다.'),
    ('local-cash', '현지 통화', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '카드 사용이 어려운 상황의 소액 결제에 사용할 수 있습니다.'),
    ('wallet', '지갑', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '현금과 결제 수단을 보관합니다.'),
    ('emergency-contacts', '긴급 연락처', 'DOCUMENTS_FINANCE', 'PERSONAL', 'FIXED', 1, 1, '분실이나 긴급 상황에서 연락할 정보를 확인할 수 있습니다.'),

    ('underwear', '속옷', 'CLOTHING', 'PERSONAL', 'DAILY_CAP', 1, 14, '매일 교체할 수 있도록 여행 일수와 여분을 고려합니다.'),
    ('socks', '양말', 'CLOTHING', 'PERSONAL', 'DAILY_CAP', 1, 14, '매일 교체할 수 있도록 여행 일수와 여분을 고려합니다.'),
    ('tops', '상의', 'CLOTHING', 'PERSONAL', 'ROTATION', 2, 5, '돌려 입을 수 있도록 여행 기간에 맞춰 준비합니다.'),
    ('bottoms', '하의', 'CLOTHING', 'PERSONAL', 'ROTATION', 3, 5, '돌려 입을 수 있도록 여행 기간에 맞춰 준비합니다.'),
    ('sleepwear', '잠옷', 'CLOTHING', 'PERSONAL', 'ROTATION', 2, 4, '여행 기간에 맞춰 갈아입을 잠옷을 준비합니다.'),
    ('warm-jacket', '방한 외투', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 2, '낮은 기온에 체온을 유지하는 데 도움이 됩니다.'),
    ('rain-jacket', '방수 외투', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 1, '비가 예상되는 날 옷이 젖는 것을 줄여줍니다.'),
    ('swimsuit', '수영복', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 2, '수영이나 물놀이 일정에 사용할 수 있습니다.'),
    ('walking-shoes', '편한 신발', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 2, '여행 중 장시간 이동할 때 발의 부담을 줄여줍니다.'),
    ('trekking-shoes', '등산화', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 1, '트레킹 활동에 적합한 신발을 준비합니다.'),
    ('hat', '모자', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 1, '햇볕이 강한 야외 활동에서 머리를 보호합니다.'),
    ('scarf', '목도리', 'CLOTHING', 'PERSONAL', 'FIXED', 1, 1, '추운 날 목 주변을 보온할 수 있습니다.'),

    ('toothbrush', '칫솔', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '여행 중 구강 위생 관리에 사용합니다.'),
    ('toothpaste', '치약', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '여행 중 구강 위생 관리에 사용합니다.'),
    ('shampoo', '샴푸', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '숙소에서 제공되지 않을 경우 사용할 수 있습니다.'),
    ('conditioner', '린스·컨디셔너', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '숙소에서 제공되지 않을 경우 사용할 수 있습니다.'),
    ('body-wash', '바디워시', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '숙소에서 제공되지 않을 경우 사용할 수 있습니다.'),
    ('face-wash', '세안제', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '평소 사용하는 세안제를 챙길 수 있습니다.'),
    ('moisturizer', '보습제', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 1, '건조한 환경에서 피부 보습에 사용할 수 있습니다.'),
    ('sunscreen', '선크림', 'TOILETRIES_HYGIENE', 'PERSONAL', 'FIXED', 1, 2, '자외선 노출이 있는 야외 활동에서 사용할 수 있습니다.'),
    ('wet-wipes', '물티슈', 'TOILETRIES_HYGIENE', 'PERSONAL', 'CONSUMABLE', 4, 5, '외출 중 손이나 물건을 닦을 때 사용할 수 있습니다.'),
    ('tissues', '휴지', 'TOILETRIES_HYGIENE', 'SHARED', 'FIXED', 1, 1, '이동 중이나 휴지가 없는 장소에서 사용할 수 있습니다.'),

    ('phone', '휴대전화', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 1, '연락, 지도, 예약 정보 확인에 사용합니다.'),
    ('phone-charger', '휴대전화 충전기', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 1, '휴대전화 배터리를 충전합니다.'),
    ('charging-cable', '충전 케이블', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 2, '기기를 충전할 때 필요합니다.'),
    ('power-bank', '보조 배터리', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 1, '기내 반입 수량과 보관 기준은 출발 전 공항·항공사 최신 안내를 확인하세요.'),
    ('earphones', '이어폰', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 1, '이동 중 오디오를 듣거나 통화할 때 사용할 수 있습니다.'),
    ('camera', '카메라', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 1, '사진 촬영이 필요한 경우 사용할 수 있습니다.'),
    ('camera-battery', '카메라 배터리', 'ELECTRONICS', 'PERSONAL', 'FIXED', 1, 2, '카메라 사용 시간이 길어질 때 예비 전원을 제공합니다.'),
    ('multi-outlet', '멀티탭', 'ELECTRONICS', 'SHARED', 'FIXED', 1, 1, '숙소에서 여러 기기를 충전할 때 사용할 수 있습니다.'),

    ('prescription-medicine', '처방약', 'MEDICINE_HEALTH', 'PERSONAL', 'FIXED', 1, 1, '평소 복용하는 처방약을 여행 기간에 맞춰 준비합니다.'),
    ('personal-medicine', '상비약', 'MEDICINE_HEALTH', 'PERSONAL', 'FIXED', 1, 1, '개인 건강 상태에 맞는 상비약을 준비할 수 있습니다.'),
    ('pain-reliever', '진통제', 'MEDICINE_HEALTH', 'PERSONAL', 'FIXED', 1, 1, '두통이나 통증이 있을 때 사용할 수 있습니다.'),
    ('motion-sickness-medicine', '멀미약', 'MEDICINE_HEALTH', 'PERSONAL', 'FIXED', 1, 1, '이동 중 멀미에 대비할 수 있습니다.'),
    ('bandages', '밴드', 'MEDICINE_HEALTH', 'SHARED', 'FIXED', 1, 1, '작은 상처를 보호할 때 사용할 수 있습니다.'),
    ('disinfectant-wipes', '소독용 물티슈', 'MEDICINE_HEALTH', 'SHARED', 'FIXED', 1, 1, '간단한 위생 관리에 사용할 수 있습니다.'),
    ('glasses', '안경', 'MEDICINE_HEALTH', 'PERSONAL', 'FIXED', 1, 1, '시력 교정이 필요한 경우 준비합니다.'),

    ('day-bag', '보조 가방', 'SAFETY_OTHER', 'PERSONAL', 'FIXED', 1, 1, '당일 외출에 필요한 물건을 나누어 휴대합니다.'),
    ('luggage-lock', '여행 가방 자물쇠', 'SAFETY_OTHER', 'PERSONAL', 'FIXED', 1, 1, '수하물을 보관할 때 잠금 장치로 사용할 수 있습니다.'),
    ('zip-bags', '지퍼백', 'SAFETY_OTHER', 'SHARED', 'FIXED', 1, 1, '작은 물건이나 젖은 물건을 분리해 보관합니다.'),
    ('umbrella', '접이식 우산', 'SAFETY_OTHER', 'SHARED', 'FIXED', 1, 1, '비가 올 때 이동 중 사용할 수 있습니다.'),
    ('sunglasses', '선글라스', 'SAFETY_OTHER', 'PERSONAL', 'FIXED', 1, 1, '햇볕이 강한 야외에서 눈부심을 줄입니다.'),
    ('water-bottle', '물병', 'SAFETY_OTHER', 'PERSONAL', 'FIXED', 1, 1, '이동 중 물을 휴대할 수 있습니다.'),
    ('travel-adapter', '멀티 어댑터', 'SAFETY_OTHER', 'SHARED', 'FIXED', 1, 1, '목적지 플러그가 한국 플러그와 다를 때 사용할 수 있습니다.')
ON CONFLICT (item_id) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    scope = EXCLUDED.scope,
    quantity_rule_type = EXCLUDED.quantity_rule_type,
    quantity_rule_parameter = EXCLUDED.quantity_rule_parameter,
    max_quantity = EXCLUDED.max_quantity,
    reason_template = EXCLUDED.reason_template;

INSERT INTO checklist_item_alias (item_id, alias) VALUES
    ('passport-copy', '여권 복사본'),
    ('flight-ticket', '탑승권'),
    ('payment-card', '신용카드'),
    ('local-cash', '현금'),
    ('underwear', '속옷'),
    ('tops', '상의'),
    ('bottoms', '하의'),
    ('sleepwear', '잠옷'),
    ('warm-jacket', '방한복'),
    ('walking-shoes', '운동화'),
    ('toothbrush', '칫솔'),
    ('wet-wipes', '휴대용 물티슈'),
    ('power-bank', '보조배터리'),
    ('phone-charger', '휴대폰 충전기'),
    ('prescription-medicine', '복용약'),
    ('personal-medicine', '개인 상비약'),
    ('zip-bags', '지퍼백'),
    ('travel-adapter', '멀티 어댑터')
ON CONFLICT DO NOTHING;

INSERT INTO checklist_item_required_tag (item_id, tag) VALUES
    ('warm-jacket', 'temp:cold'),
    ('rain-jacket', 'weather:rain'),
    ('swimsuit', 'activity:swimming'),
    ('trekking-shoes', 'activity:trekking'),
    ('hat', 'weather:uv-protection'),
    ('scarf', 'temp:cold'),
    ('sunscreen', 'weather:uv-protection'),
    ('camera', 'personal:camera'),
    ('camera-battery', 'personal:camera'),
    ('glasses', 'personal:glasses'),
    ('motion-sickness-medicine', 'activity:long-transfer'),
    ('sunglasses', 'weather:uv-protection'),
    ('umbrella', 'weather:rain'),
    ('travel-adapter', 'power:plug-mismatch')
ON CONFLICT DO NOTHING;

INSERT INTO checklist_item_exclude_tag (item_id, tag) VALUES
    ('swimsuit', 'activity:no-water-activities'),
    ('shampoo', 'lodging:shampoo-provided'),
    ('conditioner', 'lodging:conditioner-provided'),
    ('body-wash', 'lodging:body-wash-provided')
ON CONFLICT DO NOTHING;



-- 국가 전원 기준. 공식 출처, 확인일과 지역 예외를 함께 시드한다.
INSERT INTO country_power_supply (country_code, voltage_v, frequency_hz, note, source_url, checked_on) VALUES
    ('JP', 100, 50, '동일본(도쿄 포함)은 50Hz, 서일본(나고야·교토·오사카 등)은 60Hz.', 'https://www.sg.emb-japan.go.jp/itpr_en/japaninfo_electricity.html', '2026-11-13'),
    ('JP', 100, 60, '동일본(도쿄 포함)은 50Hz, 서일본(나고야·교토·오사카 등)은 60Hz.', 'https://www.sg.emb-japan.go.jp/itpr_en/japaninfo_electricity.html', '2026-11-13'),
    ('VN', 220, 50, '정부 고시 제품 시험 조건에 220V ±5%, 50Hz ±1%가 표시됩니다.', 'https://moit.gov.vn/upload/2005517/20210705/skmbt_363191211171309524.pdf', '2026-11-13'),
    ('US', 120, 60, '가정용 표준 전압. 일부 회로는 다른 전압을 사용합니다.', 'https://www.energy.gov/node/1508886', '2026-11-13'),
    ('TH', 220, 50, '', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TW', 110, 60, '일반 가정 조명·소형기기 기준. 일부 설비는 220V를 사용합니다.', 'https://law.moea.gov.tw/LawContent.aspx?id=FL011044', '2026-11-13'),
    ('TW', 220, 60, '일부 건물의 에어컨 등에서 사용합니다.', 'https://law.moea.gov.tw/LawContent.aspx?id=FL011044', '2026-11-13');

INSERT INTO country_power_plug (country_code, plug_type, source_url, checked_on) VALUES
    ('JP', 'A', 'https://www.sg.emb-japan.go.jp/itpr_en/japaninfo_electricity.html', '2026-11-13'),
    ('VN', 'A', 'https://www.itu.int/dms_pub/itu-r/md/00/sg05/cir/R00-SG05-CIR-0019%21%21PDF-E.pdf', '2026-11-13'),
    ('VN', 'C', 'https://www.itu.int/dms_pub/itu-r/md/00/sg05/cir/R00-SG05-CIR-0019%21%21PDF-E.pdf', '2026-11-13'),
    ('US', 'A', 'https://www.energy.gov/node/1508886', '2026-11-13'),
    ('US', 'B', 'https://www.energy.gov/node/1508886', '2026-11-13'),
    ('TH', 'A', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TH', 'B', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TH', 'C', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TH', 'F', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TH', 'O', 'https://www.am2026thailand.go.th/electrical-info-and-devices', '2026-11-13'),
    ('TW', 'A', 'https://travel.tycg.gov.tw/en/Service/Electricity', '2026-11-13'),
    ('TW', 'B', 'https://travel.tycg.gov.tw/en/Service/Electricity', '2026-11-13');

COMMIT;
