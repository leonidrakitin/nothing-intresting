package ru.sushi.delivery.kds.view;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.BroadcastListener;
import ru.sushi.delivery.kds.service.listeners.OrderChangesListener;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Route("screen")
@UIScope
public class ChefScreenView extends HorizontalLayout implements HasUrlParameter<String>, BroadcastListener {

    public static final int GRID_SIZE = 3;
    private final ViewService viewService;
    private final OrderChangesListener orderChangesListener;

    // 6 колонок (ячейки)
    private final VerticalLayout[] columns;
    private String screenId;
    private Long stationId;

    @Autowired
    public ChefScreenView(
            ViewService viewService,
            OrderChangesListener orderChangesListener
    ) {
        this.viewService = viewService;
        this.orderChangesListener = orderChangesListener;

        setSizeFull();

       //  Инициализируем 6 "вертикальных колонок"
        columns = new VerticalLayout[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            VerticalLayout col = new VerticalLayout();
            col.setWidth(String.format("%f%%", 100.0/GRID_SIZE));  // примерно 1/6 ширины
            col.setSpacing(true);
            columns[i] = col;
            add(col);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String screenId) {

        Optional<Long> stationId = this.viewService.getScreenStationIfExists(screenId);
        if (stationId.isEmpty()) {
            removeAll();
            add(new H1("Страница не найдена"));
            return;
        }
        this.screenId = screenId;
        this.stationId = stationId.get();
        refreshPage();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.orderChangesListener.register(stationId, this);
        refreshPage();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        this.orderChangesListener.unregister(stationId);
        super.onDetach(detachEvent);
    }

    // Этот метод вызывается, когда получаем broadcast "new order"
    @Override
    public void receiveBroadcast(BroadcastMessage message) {
        UI ui = getUI().orElse(null);
        if (ui == null) {
            return;
        }

        ui.access(() -> {
            if (message.getType() == BroadcastMessageType.REFRESH_PAGE) {
                refreshPage();
            } else if (message.getType() == BroadcastMessageType.NOTIFICATION) {
                Notification.show(message.getContent());
                ui.getPage().executeJs(
                        "new Audio($0).play();",
                        "https://commondatastorage.googleapis.com/codeskulptor-assets/jump.ogg"
                );
            }
        });
    }

    public void refreshPage() {
        for (VerticalLayout col : columns) {
            col.removeAll();
        }

        List<OrderItemDto> orders = this.viewService.getScreenOrderItems(this.screenId);
        if (orders == null || orders.isEmpty()) {
            columns[0].add("Заказов нет");
            return;
        }

        int index = 0;
        for (OrderItemDto order : orders) {
            VerticalLayout col = columns[index % GRID_SIZE];
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

        title.getStyle().set("font-weight", "bold");
        // Время готовки: текущее время - время создания
        long seconds = Duration.between(item.getCreatedAt(), Instant.now()).toSeconds();
        Div timer = new Div();

        if (item.getStatus() == OrderItemStationStatus.STARTED) {
            container.getStyle().set("background-color", "lightblue");
            timer.add("Время готовки: ");
        } else {
            timer.add("Время ожидания: ");
        }

        if (seconds > 10) {
            container.getStyle().set("background-color", "orange");
        }

        timer.add(new Text(seconds + " сек"));
        timer.getStyle().set("font-weight", "bold");
        container.add(title, details, timer);

        container.addClickListener(e -> {
            this.viewService.updateStatus(item.getId());
            refreshPage();
        });

        return container;
    }
}
