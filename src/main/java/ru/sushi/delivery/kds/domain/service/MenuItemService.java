package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemDto;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.MenuItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final FlowRepository flowRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<MenuItemDto> getAllMenuItemsDTO() {
        return menuItemRepository.findAll().stream().map(MenuItemDto::of).toList();
    }

    public MenuItem getMenuItemById(Long menuId) {
        return menuItemRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("MenuItem not found id " + menuId));
    }

    public void saveMenuItem(MenuItemDto menuItemDTO) {
        Flow flow = flowRepository.getFlowByName(menuItemDTO.getFlow())
                .orElseThrow(() -> new NotFoundException("Flow not found id " + menuItemDTO.getFlow()));

        menuItemRepository.save(MenuItem.builder()
                .id(menuItemDTO.getId())
                .name(menuItemDTO.getName())
                .flow(flow).build()
        );
    }
}
