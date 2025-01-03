package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CollectorView: показывает ВСЕ заказы и все их позиции,
 * у каждой позиции — станция; если она на станции сборки,
 * включаем "двойной клик", если нет — блокируем.
 */
@Route("collector")
public class CollectorView extends VerticalLayout {

    private final ViewService viewService;

    /**
     * Храним «состояние клика» (подсветки) по позициям.
     * key = orderItemId, value = true, если подсвечена (ожидает второго клика).
     */
    private final Map<Long, Boolean> itemClickedState = new HashMap<>();

    @Autowired
    public CollectorView(ViewService viewService) {
        this.viewService = viewService;
        setSizeFull();
        getStyle().set("padding", "20px");
        getStyle().set("overflow", "auto");

        refreshPage();
    }

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

        // Расстояния между колонками
        flex.getStyle().set("gap", "20px");
        flex.getStyle().set("padding", "10px");

        for (OrderFullDto orderDto : allOrders) {
            VerticalLayout orderColumn = new VerticalLayout();
            orderColumn.setSpacing(false);
            orderColumn.setPadding(true);
            orderColumn.setWidth("300px");
            orderColumn.setHeight(null);

            orderColumn.getStyle().set("border", "1px solid #ccc");
            orderColumn.getStyle().set("border-radius", "8px");
            orderColumn.getStyle().set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)");

            H2 orderHeader = new H2("Заказ #" + orderDto.getOrderId());
            orderHeader.getStyle().set("margin", "10px 10px 0 10px");
            orderColumn.add(orderHeader);

            // Добавляем «плитки»
            for (OrderItemDto item : orderDto.getItems()) {
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
     *  - Если станция == "СБОР ЗАКАЗА" (или "COLLECT"), включаем "двойной клик".
     *  - Иначе делаем плитку полупрозрачной и отключаем клик.
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

        // Время
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

        // ЛОГИКА «двух кликов» только для станции сборки
        // Если у вас называется "COLLECT", то проверяем "COLLECT" (equalsIgnoreCase).
        // Сейчас пример "СБОР ЗАКАЗА".
        if ("СБОР ЗАКАЗА".equalsIgnoreCase(item.getCurrentStation().getName())) {
            // Делаем плитку кликабельной
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

                    // Обновить список, чтобы позиция исчезла или станция сменилась
                    refreshPage();
                }
            });
        } else {
            // Если позиция не на станции сборки, блокируем клики
            tile.getStyle().set("cursor", "default");
            tile.getStyle().set("opacity", "0.7");
        }

        return tile;
    }
}

