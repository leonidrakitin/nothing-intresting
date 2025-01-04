package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.BroadcastListener;
import ru.sushi.delivery.kds.service.listeners.OrderChangesListener;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Показывает все заказы и все их позиции (плитки).
 * - Если станция == "СБОР ЗАКАЗА", есть режим «двух кликов» (подсветка, затем "собрано").
 * - Иначе плитка полупрозрачная.
 * - Заказы, в которых после фильтрации нет позиций, не отображаются.
 */
@Route("collector")
public class CollectorView extends VerticalLayout implements BroadcastListener {

    private final ViewService viewService;
    private final OrderChangesListener orderChangesListener;

    /**
     * Храним «состояние клика» (подсветки) по позициям.
     * key = orderItemId, value = true, если подсвечена (ожидает второго клика).
     */
    private final Map<Long, Boolean> itemClickedState = new HashMap<>();

    @Autowired
    public CollectorView(ViewService viewService, OrderChangesListener orderChangesListener) {
        this.viewService = viewService;
        this.orderChangesListener = orderChangesListener;
        setSizeFull();
        getStyle().set("padding", "20px");
        getStyle().set("overflow", "auto");

        refreshPage();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Регистрируемся на слушателя, например, c ID=3 (или любой другой)
        this.orderChangesListener.register(3, this);
        refreshPage();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        this.orderChangesListener.unregister(3);
        super.onDetach(detachEvent);
    }

    /**
     * Получаем broadcast-сообщения.
     * Например, NEW_ORDER -> показываем уведомление о новом заказе.
     * REFRESH_PAGE -> обновляем UI.
     * NOTIFICATION -> просто показываем сообщение.
     */
    @Override
    public void receiveBroadcast(BroadcastMessage message) {
        UI ui = getUI().orElse(null);
        if (ui == null) {
            return;
        }

        ui.access(() -> {
            switch (message.getType()) {
                case REFRESH_PAGE -> {
                    // Обновляем список
                    refreshPage();
                }
                case NOTIFICATION -> {
                    // Покажем всплывающее уведомление
                    Notification.show(message.getContent());
                    // Например, проиграем звук
                    ui.getPage().executeJs(
                        "new Audio($0).play();",
                        "https://commondatastorage.googleapis.com/codeskulptor-assets/jump.ogg"
                    );
                }
            }
        });
    }

    /**
     * Перерисовываем страницу (список заказов -> плиток).
     */
    private void refreshPage() {
        removeAll();
        itemClickedState.clear();

        List<OrderFullDto> allOrders = viewService.getAllOrdersWithItems();
        if (allOrders.isEmpty()) {
            add(new H2("Нет заказов в системе"));
            return;
        }

        // FlexLayout для колонок
        FlexLayout flex = new FlexLayout();
        flex.setWidthFull();
        flex.setHeight(null);

        // Горизонтальная прокрутка и отсутствие переноса
        flex.getStyle().set("display", "flex");
        flex.getStyle().set("flex-direction", "row");
        flex.getStyle().set("flex-wrap", "nowrap");
        flex.getStyle().set("overflow-x", "auto");
        flex.getStyle().set("white-space", "nowrap");

        // ВАЖНО: прижимаем колонки к верху, чтобы высота шла по содержимому
        flex.getStyle().set("align-items", "flex-start");

        // Расстояние между колонками
        flex.getStyle().set("gap", "20px");
        flex.getStyle().set("padding", "10px");

        for (OrderFullDto orderDto : allOrders) {

            // Фильтруем позиции в заказе:
            // 1) исключаем те, что CANCELED
            // 2) исключаем те, где station = READY
            List<OrderItemDto> filteredItems = orderDto.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .filter(item -> item.getCurrentStation().getOrderStatusAtStation() != OrderStatus.READY)
                .toList();

            // Если список отфильтрованных позиций пуст — пропускаем заказ (не рисуем колонку)
            if (filteredItems.isEmpty()) {
                continue;
            }

            VerticalLayout orderColumn = new VerticalLayout();
            orderColumn.setSpacing(false);
            orderColumn.setPadding(true);
            orderColumn.setWidth("300px");
            orderColumn.setHeight(null);

            orderColumn.getStyle().set("border", "1px solid #ccc");
            orderColumn.getStyle().set("border-radius", "8px");
            orderColumn.getStyle().set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)");

            // Заголовок колонки
            H2 orderHeader = new H2("Заказ #" + orderDto.getOrderId());
            orderHeader.getStyle().set("margin", "10px 10px 0 10px");
            orderColumn.add(orderHeader);

            // Рисуем плитки для каждого элемента
            for (OrderItemDto item : filteredItems) {
                Div tile = buildItemTile(item);
                orderColumn.add(tile);
            }

            flex.add(orderColumn);
        }
        add(flex);
    }

    /**
     * Создаём «плитку» для позиции:
     *  - Показываем её название, станцию, время.
     *  - Если станция == "СБОР ЗАКАЗА" — «двойной клик» (подсветка, второй клик = updateStatus).
     *  - Иначе плитка полупрозрачная и не кликабельна.
     */
    private Div buildItemTile(OrderItemDto item) {
        Div tile = new Div();
        tile.setWidth("100%");
        tile.getStyle().set("border", "1px solid #ddd");
        tile.getStyle().set("padding", "1px");
        tile.getStyle().set("margin", "5px 20px 5px 0px");

        tile.getStyle().set("border-radius", "1px");
        tile.getStyle().set("background-color", "#fafafa");
        tile.getStyle().set("transition", "background-color 0.3s ease-in-out");
        tile.getStyle().set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        // Если уже подсвечено
        if (Boolean.TRUE.equals(itemClickedState.get(item.getId()))) {
            tile.getStyle().set("background-color", "#fff0b3");
        }

        // Заголовок
        Div title = new Div(new Text("Позиция: " + item.getName()));
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("margin-bottom", "5px");

        // Станция
        Div stationDiv = new Div(new Text("Станция: " + item.getCurrentStation().getName()));

        // Время (секунды с момента createdAt)
        long seconds = Duration.between(item.getCreatedAt(), Instant.now()).toSeconds();
        Div timeDiv = new Div(new Text("Прошло: " + seconds + " сек"));
        timeDiv.getStyle().set("font-size", "0.9em");
        timeDiv.getStyle().set("color", "#777");

        tile.add(title, stationDiv, timeDiv);

        // Ингредиенты (опционально)
        if (item.getIngredients() != null && !item.getIngredients().isEmpty()) {
            Div ingredientsDiv = new Div();
            for (String ingr : item.getIngredients()) {
                ingredientsDiv.add(new Div(new Text("- " + ingr)));
            }
            ingredientsDiv.getStyle().set("font-size", "0.9em");
            ingredientsDiv.getStyle().set("color", "#555");
            tile.add(ingredientsDiv);
        }

        // Логика «двух кликов» только для "СБОР ЗАКАЗА" (пример)
        if ("СБОР ЗАКАЗА".equalsIgnoreCase(item.getCurrentStation().getName())) {
            tile.getStyle().set("cursor", "pointer");

            tile.addClickListener(e -> {
                Boolean isHighlighted = itemClickedState.get(item.getId());
                if (isHighlighted == null || !isHighlighted) {
                    // Первый клик => подсветим
                    tile.getStyle().set("background-color", "#fff0b3");
                    itemClickedState.put(item.getId(), true);
                } else {
                    // Второй клик => updateStatus
                    viewService.updateStatus(item.getId());
                    Notification.show("Позиция собрана: " + item.getName());
                    itemClickedState.remove(item.getId());

                    // Обновить список
                    refreshPage();
                }
            });
        } else {
            tile.getStyle().set("cursor", "default");
            tile.getStyle().set("opacity", "0.7");
        }

        return tile;
    }
}
