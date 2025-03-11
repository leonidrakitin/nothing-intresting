package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.repository.ItemComboRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemComboService {

    private final ItemComboRepository itemComboRepository;

    public List<ItemCombo> findAll() {
        return this.itemComboRepository.findAll();
    }
}
