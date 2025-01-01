package ru.sushi.delivery.kds.view;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Route("screen")
public class ChefScreenView extends HorizontalLayout implements HasUrlParameter<String> {

    private final ViewService viewService;

    // 6 колонок (ячейки)
    private final VerticalLayout[] columns;
    private String currentScreenId;

    @Autowired
    public ChefScreenView(ViewService viewService) {
        this.viewService = viewService;

        setSizeFull();

       //  Инициализируем 6 "вертикальных колонок"
        columns = new VerticalLayout[5];
        for (int i = 0; i < 5; i++) {
            VerticalLayout col = new VerticalLayout();
            col.setWidth("20.0%");  // примерно 1/6 ширины
            col.setSpacing(true);
            columns[i] = col;
            add(col);
        }

        // Регулярно (каждые X секунд) обновлять UI (pull) –
        // чтобы «свежие» заказы отобразились без перезагрузки страницы.
        // Это один из вариантов (Vaadin Poll); другой – Push (WebSocket).
        getUI().ifPresent(ui -> {
            ui.access(this::refreshPage);
            ui.setPollInterval(1000); // каждые 5 сек
        });
        // не работает??
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String screenId) {

        if (!this.viewService.checkScreenExists(screenId)) {
            removeAll();
            add(new H1("Страница не найдена"));
            return;
        }
        this.currentScreenId = screenId;
        refreshPage();
    }

    // Переопределяем метод, чтобы при каждом "poll" обновлять заказы
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        refreshPage();
        ui.addPollListener(e -> refreshPage());
    }

    public void refreshPage() {

        for (VerticalLayout col : columns) {
            col.removeAll();
        }

        List<OrderItemDto> orders = this.viewService.getScreenOrderItems(this.currentScreenId);
        if (orders == null || orders.isEmpty()) {
            columns[0].add("Заказов нет");
            return;
        }

        int index = 0;
        for (OrderItemDto order : orders) {
            VerticalLayout col = columns[index % 6];
            index++;
            col.add(buildOrderComponent(order));
        }
    }

    private Div buildOrderComponent(OrderItemDto item) {
        Div container = new Div();
        container.getStyle().set("border", "1px solid #ccc");
        container.getStyle().set("padding", "10px");
        container.getStyle().set("margin", "5px");
        container.setWidthFull();

        // Заголовок: "Заказ #id: <имя>"
        Div title = new Div(new Text("Заказ #" + item.getOrderId() + ": " + item.getName()));
        Div details = new Div();
        for (var ingredient : item.getIngredients()) {
            details.add(new Div(new Text("- " + ingredient)));
        }
        // Время готовки: текущее время - время создания
        long seconds = Duration.between(item.getCreatedAt(), Instant.now()).toSeconds();
        Div timer = new Div(new Text("Время готовки: " + seconds + " сек"));

        title.getStyle().set("font-weight", "bold");
        timer.getStyle().set("font-weight", "bold");

        container.add(title, details, timer);

        container.addClickListener(e -> {
            this.viewService.updateStatus(item.getId());
            refreshPage();
        });

        return container;
    }
}
