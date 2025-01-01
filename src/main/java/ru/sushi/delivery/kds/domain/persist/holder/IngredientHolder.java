package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Ingredient;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;
import ru.sushi.delivery.kds.domain.util.IngredientsCatalog;

@Component
public class IngredientHolder extends AbstractInMemoryHolder<Ingredient, Long> {
    @PostConstruct
    public void init() {
        load(BusinessLogic.ALL_INGREDIENTS);
    }
}
