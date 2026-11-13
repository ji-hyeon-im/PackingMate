-- 기본 체크리스트 항목과 규칙에 필요한 정규화 스키마.
-- PostgreSQL용이며, 스크립트를 반복 실행해도 기존 테이블을 유지한다.

CREATE TABLE IF NOT EXISTS checklist_item (
    item_id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(40) NOT NULL CHECK (category IN (
        'DOCUMENTS_FINANCE',
        'CLOTHING',
        'TOILETRIES_HYGIENE',
        'ELECTRONICS',
        'MEDICINE_HEALTH',
        'SAFETY_OTHER'
    )),
    scope VARCHAR(20) NOT NULL CHECK (scope IN ('PERSONAL', 'SHARED')),
    quantity_rule_type VARCHAR(20) NOT NULL CHECK (quantity_rule_type IN (
        'FIXED', 'DAILY_CAP', 'ROTATION', 'CONSUMABLE'
    )),
    -- FIXED: 고정 수량, DAILY_CAP: 여분 수량, ROTATION/CONSUMABLE: 수량당 일수
    quantity_rule_parameter INTEGER NOT NULL CHECK (quantity_rule_parameter >= 0),
    max_quantity INTEGER NOT NULL CHECK (max_quantity >= 1),
    reason_template VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT checklist_item_rule_parameter_check CHECK (
        (quantity_rule_type = 'FIXED' AND quantity_rule_parameter >= 1)
        OR (quantity_rule_type = 'DAILY_CAP' AND quantity_rule_parameter >= 0)
        OR (quantity_rule_type IN ('ROTATION', 'CONSUMABLE') AND quantity_rule_parameter >= 1)
    )
);

CREATE TABLE IF NOT EXISTS checklist_item_alias (
    item_id VARCHAR(100) NOT NULL REFERENCES checklist_item(item_id) ON DELETE CASCADE,
    alias VARCHAR(150) NOT NULL,
    PRIMARY KEY (item_id, alias),
    CHECK (length(trim(alias)) > 0)
);

CREATE TABLE IF NOT EXISTS checklist_item_required_tag (
    item_id VARCHAR(100) NOT NULL REFERENCES checklist_item(item_id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (item_id, tag),
    CHECK (length(trim(tag)) > 0)
);

CREATE TABLE IF NOT EXISTS checklist_item_exclude_tag (
    item_id VARCHAR(100) NOT NULL REFERENCES checklist_item(item_id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (item_id, tag),
    CHECK (length(trim(tag)) > 0)
);

CREATE TABLE IF NOT EXISTS country_power_supply (
    country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    voltage_v SMALLINT NOT NULL CHECK (voltage_v > 0),
    frequency_hz SMALLINT NOT NULL CHECK (frequency_hz > 0),
    note VARCHAR(250) NOT NULL DEFAULT '',
    source_url VARCHAR(500) NOT NULL CHECK (length(trim(source_url)) > 0),
    checked_on DATE NOT NULL,
    PRIMARY KEY (country_code, voltage_v, frequency_hz),
    CHECK (source_url LIKE 'https://%')
);

CREATE TABLE IF NOT EXISTS country_power_plug (
    country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    plug_type VARCHAR(1) NOT NULL CHECK (plug_type IN ('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O')),
    source_url VARCHAR(500) NOT NULL CHECK (length(trim(source_url)) > 0),
    checked_on DATE NOT NULL,
    PRIMARY KEY (country_code, plug_type),
    CHECK (source_url LIKE 'https://%')
);

CREATE INDEX IF NOT EXISTS idx_checklist_item_category_scope
    ON checklist_item (category, scope);
CREATE INDEX IF NOT EXISTS idx_checklist_item_required_tag_tag
    ON checklist_item_required_tag (tag);
CREATE INDEX IF NOT EXISTS idx_checklist_item_exclude_tag_tag
    ON checklist_item_exclude_tag (tag);
