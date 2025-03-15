package ru.sushi.delivery.kds.view.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

@AllArgsConstructor
@Setter
@Getter
public class CartItem {

    private final MenuItem menuItem;
    private int quantity;

    public void increment() {
        this.quantity++;
    }

    public void decrement() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }
}
