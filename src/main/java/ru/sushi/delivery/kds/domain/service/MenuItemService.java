package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.MenuItemRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final FlowRepository flowRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> getAllExtras() {
        return menuItemRepository.findAllExtras();
    }

    public List<MenuItemData> getAllMenuItemsDTO() {
        return menuItemRepository.findAll().stream().map(MenuItemData::of).toList();
    }

    public MenuItem getMenuItemById(Long menuId) {
        return menuItemRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("MenuItem not found id " + menuId));
    }

    public void deleteMenuItem(MenuItemData menuItemData) {
        this.menuItemRepository.deleteById(menuItemData.getId());
    }

    public void saveMenuItem(MenuItemData menuItemData) {
        Flow flow = flowRepository.getFlowByName(menuItemData.getFlow())
                .orElseThrow(() -> new NotFoundException("Flow not found id " + menuItemData.getFlow()));

        MenuItem menuItem = Optional.ofNullable(menuItemData.getId())
                .map(this.menuItemRepository::findById)
                .flatMap(Function.identity())
                .map(m -> this.setNewMenuItemData(m, menuItemData, flow))
                .orElseGet(() -> MenuItem.of(menuItemData, flow));

        menuItemRepository.save(menuItem);
    }

    public MenuItem setNewMenuItemData(
            MenuItem menuItem,
            MenuItemData menuItemData,
            Flow flow
    ) {
        return menuItem.toBuilder()
                .id(menuItem.getId())
                .name(menuItemData.getName())
                .flow(flow)
                .build();
    }
}
