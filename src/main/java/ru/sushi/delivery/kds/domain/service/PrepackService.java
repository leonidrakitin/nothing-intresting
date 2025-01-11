package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;

@Service
@RequiredArgsConstructor
public class PrepackService {

    private final PrepackRepository prepackRepository;

    public Prepack get(Long id) {
        return this.prepackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prepack not found id " + id));
    }
}
