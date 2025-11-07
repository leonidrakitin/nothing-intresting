package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItemAlias;
import ru.sushi.delivery.kds.domain.persist.repository.product.MenuItemAliasRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemAliasService {

    private final MenuItemAliasRepository menuItemAliasRepository;

    @Transactional(readOnly = true)
    public Optional<MenuItemAlias> findAlias(String aliasText) {
        if (aliasText == null) {
            return Optional.empty();
        }
        String trimmed = aliasText.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        return menuItemAliasRepository.findByAliasTextIgnoreCase(trimmed);
    }

    @Transactional(readOnly = true)
    public Optional<MenuItem> findMenuItemAliasTarget(String aliasText) {
        return findAlias(aliasText)
                .map(MenuItemAlias::getMenuItem)
                .map(menuItem -> {
                    // Инициируем прокси, чтобы избежать LazyInitializationException
                    menuItem.getName();
                    menuItem.getId();
                    return menuItem;
                })
                .filter(Objects::nonNull);
    }

    @Transactional(readOnly = true)
    public Optional<ItemCombo> findComboAliasTarget(String aliasText) {
        return findAlias(aliasText)
                .map(MenuItemAlias::getCombo)
                .map(combo -> {
                    combo.getName();
                    combo.getId();
                    return combo;
                })
                .filter(Objects::nonNull);
    }

    @Transactional
    public MenuItemAlias saveMenuItemAlias(String aliasText, MenuItem menuItem) {
        String sanitizedAlias = sanitizeAliasText(aliasText);
        Optional<MenuItemAlias> existing = menuItemAliasRepository.findByAliasTextIgnoreCase(sanitizedAlias);
        MenuItemAlias alias = existing.map(current -> current.toBuilder()
                        .aliasText(sanitizedAlias)
                        .menuItem(menuItem)
                        .combo(null)
                        .build())
                .orElse(MenuItemAlias.builder()
                        .aliasText(sanitizedAlias)
                        .menuItem(menuItem)
                        .build());
        return menuItemAliasRepository.save(alias);
    }

    @Transactional
    public MenuItemAlias saveComboAlias(String aliasText, ItemCombo combo) {
        String sanitizedAlias = sanitizeAliasText(aliasText);
        Optional<MenuItemAlias> existing = menuItemAliasRepository.findByAliasTextIgnoreCase(sanitizedAlias);
        MenuItemAlias alias = existing.map(current -> current.toBuilder()
                        .aliasText(sanitizedAlias)
                        .combo(combo)
                        .menuItem(null)
                        .build())
                .orElse(MenuItemAlias.builder()
                        .aliasText(sanitizedAlias)
                        .combo(combo)
                        .build());
        return menuItemAliasRepository.save(alias);
    }

    @Transactional
    public void deleteAlias(String aliasText) {
        findAlias(aliasText).ifPresent(menuItemAliasRepository::delete);
    }

    private String sanitizeAliasText(String aliasText) {
        if (aliasText == null) {
            throw new IllegalArgumentException("aliasText must not be null");
        }
        String trimmed = aliasText.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("aliasText must not be empty");
        }
        return trimmed;
    }
}

