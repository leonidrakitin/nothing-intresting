package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.ItemSet;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;

@Component
public class itemSetHolder extends AbstractInMemoryHolder<ItemSet, Long> {

    @PostConstruct
    public void init() {
        load(BusinessLogic.itemSets);
    }
}
