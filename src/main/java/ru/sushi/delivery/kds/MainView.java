package ru.sushi.delivery.kds;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.Ingredient;
import ru.sushi.delivery.kds.domain.Item;
import ru.sushi.delivery.kds.service.ChefScreenService;

import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private Item item = new Item("Филадельфия", List.of(
            new Ingredient("Соус унаги", "г", 10),
            new Ingredient("Соус унаги", "г", 10),
            new Ingredient("Шапка Лава", "г", 10),
            new Ingredient("Рис", "г", 10),
            new Ingredient("Нори", "г", 10)
    ));
    private final ChefScreenService chefScreenService;

    public MainView(@Autowired ChefScreenService chefScreenService) {
        this.chefScreenService = chefScreenService;

        setSizeFull();

        Button createScreenButton = new Button("Создать экран повара");
        Div resultDiv = new Div(); // Здесь будем выводить ссылку

        createScreenButton.addClickListener(e -> {
            // Создать новый экран
            Long screenId = chefScreenService.createNewScreen();
            // Сформировать ссылку вида http://localhost:8080/screen/<uuid>
            String link = getUI().get().getInternals().getActiveViewLocation().getPath()
                    + "screen/" + screenId.toString();
            chefScreenService.getScreenOrders(screenId).add(item);
            chefScreenService.getScreenOrders(screenId).add(item);
            // Вывести ссылку на экран
            //            resultDiv.removeAll();
            resultDiv.add(new Div("Экран создан: "));
            Anchor anchor = new Anchor(link, link);
            anchor.setTarget("_blank"); // открыть в новой вкладке
            resultDiv.add(anchor);
        });

        add(createScreenButton, resultDiv);
    }
}
