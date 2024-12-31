package ru.sushi.delivery.kds;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.Item;
import ru.sushi.delivery.kds.persist.model.ScreenSettings;
import ru.sushi.delivery.kds.service.ChefScreenService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

//@Push
@Route("screen")
public class ChefScreenView extends HorizontalLayout implements HasUrlParameter<Long> {

    private final ChefScreenService chefScreenService;

    // 6 колонок (ячейки)
    private final VerticalLayout[] columns = new VerticalLayout[6];

    private Long currentScreenId;
    private ScreenSettings screenSettings;

    @Autowired
    public ChefScreenView(ChefScreenService chefScreenService) {
        this.chefScreenService = chefScreenService;
//        chefScreenService.generateOrderEveryMinute();

        setSizeFull();

       //  Инициализируем 6 "вертикальных колонок"
        for (int i = 0; i < 6; i++) {
            VerticalLayout col = new VerticalLayout();
            col.setWidth("16.6%");  // примерно 1/6 ширины
            col.setSpacing(true);
            columns[i] = col;
            add(col);
        }

        // Регулярно (каждые X секунд) обновлять UI (pull) –
        // чтобы «свежие» заказы отобразились без перезагрузки страницы.
        // Это один из вариантов (Vaadin Poll); другой – Push (WebSocket).
        getUI().ifPresent(ui -> {
            ui.setPollInterval(5000); // каждые 5 сек
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long screenId) {

        this.screenSettings = chefScreenService.getScreenSettings(screenId);
        if (screenSettings == null) {
            add("Страница не найдена");
            return;
        }
        this.currentScreenId = screenId;
        refreshOrders();
    }

    // Переопределяем метод, чтобы при каждом "poll" обновлять заказы
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        refreshOrders();
        ui.addPollListener(e -> refreshOrders());
    }

    private void refreshOrders() {

        for (VerticalLayout col : columns) {
            if (col != null) col.removeAll();
        }

        List<Item> orders = chefScreenService.getScreenOrders(this.currentScreenId);
        if (orders == null || orders.isEmpty()) {
            add("Заказов нет");
            return;
        }

        int index = 0;
        for (Item order : orders) {
            VerticalLayout col = columns[index % 6];
            index++;

            // Построим компонент, отображающий заказ
            Div orderDiv = buildOrderComponent(order);

            col.add(orderDiv);
        }
    }

    private Div buildOrderComponent(Item order) {
        Div container = new Div();
        container.getStyle().set("border", "1px solid #ccc");
        container.getStyle().set("padding", "10px");
        container.getStyle().set("margin", "5px");
        container.setWidthFull();

        // Заголовок: "Заказ #id: <имя>"
        Div title = new Div(new Text("Заказ #" + order.getId() + ": " + order.getName()));
        Div details = new Div();
        for (var ingredient : order.getIngredients()) {
            details.add(new Div(new Text("- " + ingredient.toString())));
        }
        // Время готовки: текущее время - время создания
        long seconds = Duration.between(order.getCreatedAt(), Instant.now()).toSeconds();
        Div timer = new Div(new Text("Время готовки: " + seconds + " сек"));

        title.getStyle().set("font-weight", "bold");
        timer.getStyle().set("font-weight", "bold");

        container.add(title, details, timer);

        container.addClickListener(e -> {
            chefScreenService.removeOrder(this.currentScreenId, order.getId());
            refreshOrders();
        });

        return container;
    }
}
