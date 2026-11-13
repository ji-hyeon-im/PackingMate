package com.packingmate.repository;

import com.packingmate.domain.power.PowerStandard;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL 기준 자료와 출처 확인일을 읽는다. */
@Repository
public class PowerStandardRepository {
    private final JdbcTemplate jdbcTemplate;

    public PowerStandardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PowerStandard> findByCountryCode(String countryCode) {
        List<PowerStandard.Supply> supplies = jdbcTemplate.query("""
                SELECT voltage_v, frequency_hz, note, source_url, checked_on
                FROM country_power_supply
                WHERE country_code = ?
                ORDER BY voltage_v, frequency_hz
                """, (resultSet, rowNum) -> mapSupply(resultSet), countryCode);
        List<PowerStandard.Plug> plugs = jdbcTemplate.query("""
                SELECT plug_type, source_url, checked_on
                FROM country_power_plug
                WHERE country_code = ?
                ORDER BY plug_type
                """, (resultSet, rowNum) -> mapPlug(resultSet), countryCode);
        if (supplies.isEmpty() || plugs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PowerStandard(countryCode, supplies, plugs));
    }

    private PowerStandard.Supply mapSupply(ResultSet resultSet) throws SQLException {
        return new PowerStandard.Supply(resultSet.getInt("voltage_v"), resultSet.getInt("frequency_hz"),
                resultSet.getString("note"), resultSet.getString("source_url"),
                resultSet.getDate("checked_on").toLocalDate());
    }

    private PowerStandard.Plug mapPlug(ResultSet resultSet) throws SQLException {
        return new PowerStandard.Plug(resultSet.getString("plug_type"), resultSet.getString("source_url"),
                resultSet.getDate("checked_on").toLocalDate());
    }
}
