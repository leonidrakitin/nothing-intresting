package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiCityViewService {

    @Qualifier("parnasJdbcTemplate")
    private final JdbcTemplate parnasJdbcTemplate;

    @Qualifier("ukhtaJdbcTemplate")
    private final JdbcTemplate ukhtaJdbcTemplate;

    public enum City {
        PARNAS,
        UKHTA
    }

    public List<MenuItem> getMenuItems(City city) {
        JdbcTemplate template = getTemplate(city);
        String sql = """
            SELECT mi.id, mi.name, mi.price
            FROM menu_item mi
            ORDER BY mi.name
            """;
        
        return template.query(sql, (rs, rowNum) -> {
            MenuItem item = MenuItem.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getDouble("price"))
                .build();
            // Note: productType and flow would need to be loaded separately if needed
            return item;
        });
    }

    public List<MenuItem> getExtras(City city) {
        JdbcTemplate template = getTemplate(city);
        String sql = """
            SELECT mi.id, mi.name, mi.price
            FROM menu_item mi
            JOIN product_type pt ON mi.product_type_id = pt.id
            WHERE pt.extra = true
            ORDER BY mi.name
            """;
        
        return template.query(sql, (rs, rowNum) -> {
            MenuItem item = MenuItem.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getDouble("price"))
                .build();
            return item;
        });
    }

    public List<ItemCombo> getCombos(City city) {
        JdbcTemplate template = getTemplate(city);
        String sql = """
            SELECT ic.id, ic.name
            FROM item_combo ic
            ORDER BY ic.name
            """;
        
        // First, load all combos with their menu items in one query
        Map<Long, List<MenuItem>> comboMenuItemsMap = new HashMap<>();
        
        String comboItemsSql = """
            SELECT icc.item_combo_id, mi.id, mi.name, mi.price
            FROM item_combo_compound icc
            JOIN menu_item mi ON icc.menu_item_id = mi.id
            ORDER BY icc.item_combo_id, mi.name
            """;
        
        template.query(comboItemsSql, (rs) -> {
            Long comboId = rs.getLong("item_combo_id");
            MenuItem item = MenuItem.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getDouble("price"))
                .build();
            comboMenuItemsMap.computeIfAbsent(comboId, k -> new ArrayList<>()).add(item);
        });
        
        List<ItemCombo> combos = template.query(sql, (rs, rowNum) -> {
            Long comboId = rs.getLong("id");
            List<MenuItem> menuItems = comboMenuItemsMap.getOrDefault(comboId, new ArrayList<>());
            return ItemCombo.builder()
                .id(comboId)
                .name(rs.getString("name"))
                .menuItems(menuItems)
                .build();
        });

        return combos;
    }

    private JdbcTemplate getTemplate(City city) {
        return switch (city) {
            case PARNAS -> parnasJdbcTemplate;
            case UKHTA -> ukhtaJdbcTemplate;
        };
    }
}

