package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackItemData;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrepackItemService {

    private final PrepackItemRepository prepackItemRepository;

    public PrepackItem get(Long id) {
        return prepackItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prepack item not found by id: " + id));
    }

    public boolean checkIfAlmostEmpty(Prepack prepack) {
        double amount = this.prepackItemRepository.findActiveByPrepackId(prepack.getId()).stream()
                .map(SourceItem::getAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
        return amount <= prepack.getNotifyAfterAmount();
    }

    public List<PrepackItemData> getAll() {
        return prepackItemRepository.findAll().stream()
                .map(PrepackItemData::of)
                .toList();
    }
}
