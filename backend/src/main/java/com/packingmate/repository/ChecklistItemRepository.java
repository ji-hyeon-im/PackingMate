package com.packingmate.repository;

import com.packingmate.domain.ChecklistItem;
import com.packingmate.domain.ItemCategory;
import com.packingmate.domain.ItemScope;
import com.packingmate.domain.QuantityRule;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL 초기 데이터에서 규칙 엔진용 준비물과 관련 태그를 읽는다. */
@Repository
public class ChecklistItemRepository {
    private static final String ITEM_SQL = """
            SELECT item_id, name, category, scope, quantity_rule_type,
                   quantity_rule_parameter, max_quantity, reason_template
            FROM checklist_item
            ORDER BY item_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ChecklistItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ChecklistItem> findAll() {
        List<BaseItem> baseItems = jdbcTemplate.query(ITEM_SQL, (resultSet, rowNum) -> mapBaseItem(resultSet));
        Map<String, Set<String>> aliases = loadValues("checklist_item_alias", "alias");
        Map<String, Set<String>> requiredTags = loadValues("checklist_item_required_tag", "tag");
        Map<String, Set<String>> excludeTags = loadValues("checklist_item_exclude_tag", "tag");

        return baseItems.stream()
                .map(item -> new ChecklistItem(
                        item.itemId(),
                        item.name(),
                        aliases.getOrDefault(item.itemId(), Set.of()),
                        item.category(),
                        item.scope(),
                        requiredTags.getOrDefault(item.itemId(), Set.of()),
                        excludeTags.getOrDefault(item.itemId(), Set.of()),
                        item.quantityRule(),
                        item.maxQuantity(),
                        item.reasonTemplate()))
                .toList();
    }

    private Map<String, Set<String>> loadValues(String tableName, String valueColumn) {
        // 테이블과 컬럼은 위의 내부 상수 호출만 사용하며 외부 입력으로 받지 않는다.
        String sql = "SELECT item_id, " + valueColumn + " FROM " + tableName + " ORDER BY item_id, " + valueColumn;
        Map<String, Set<String>> valuesByItemId = new LinkedHashMap<>();
        jdbcTemplate.query(sql, resultSet -> {
            valuesByItemId.computeIfAbsent(resultSet.getString("item_id"), ignored -> new LinkedHashSet<>())
                    .add(resultSet.getString(valueColumn));
        });
        return valuesByItemId;
    }

    private BaseItem mapBaseItem(ResultSet resultSet) throws SQLException {
        return new BaseItem(
                resultSet.getString("item_id"),
                resultSet.getString("name"),
                ItemCategory.valueOf(resultSet.getString("category")),
                ItemScope.valueOf(resultSet.getString("scope")),
                quantityRule(
                        resultSet.getString("quantity_rule_type"),
                        resultSet.getInt("quantity_rule_parameter")),
                resultSet.getInt("max_quantity"),
                resultSet.getString("reason_template"));
    }

    private QuantityRule quantityRule(String type, int parameter) {
        return switch (type) {
            case "FIXED" -> new QuantityRule.Fixed(parameter);
            case "DAILY_CAP" -> new QuantityRule.DailyCap(parameter);
            case "ROTATION" -> new QuantityRule.Rotation(parameter);
            case "CONSUMABLE" -> new QuantityRule.Consumable(parameter);
            default -> throw new IllegalStateException("알 수 없는 수량 규칙입니다: " + type);
        };
    }

    private record BaseItem(
            String itemId,
            String name,
            ItemCategory category,
            ItemScope scope,
            QuantityRule quantityRule,
            int maxQuantity,
            String reasonTemplate) {
    }
}
