package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;

@Component
public class ItemHolder extends AbstractInMemoryHolder<Item, Long> {
    @PostConstruct
    public void init() {
        load(BusinessLogic.items);
    }
}