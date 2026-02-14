package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    @Autowired
    public MainView(ViewService viewService) {

        setSizeFull();

        String currentTime = LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm (zzz)")
        );
        add(new Span("Текущее время: " + currentTime));

        H2 headerScreens = new H2("Экраны доступные поваров:");
        add(headerScreens);
        List<KitchenDisplayInfoDto> displayData = viewService.getAvailableDisplaysData();
        for (var display : displayData) {
            String link = "screen/" + display.getScreenId();
            Text stationName = new Text(display.getStationName() + " ");
            Anchor anchor = new Anchor(link, link);
            add(new Div(stationName, anchor));
        }

        Button goToCreateScreen = new Button("Создать новый заказ");
        goToCreateScreen.addClickListener(e ->
            UI.getCurrent().navigate("create")
        );
        add(goToCreateScreen);

        Button goToCreateOperatorScreen = new Button("Создать мульти-городский заказ");
        goToCreateOperatorScreen.addClickListener(e ->
            UI.getCurrent().navigate("create-new")
        );
        add(goToCreateOperatorScreen);

        Button goToCollectorScreen = new Button("Сборщик");
        goToCollectorScreen.addClickListener(e ->
            UI.getCurrent().navigate("collector")
        );
        add(goToCollectorScreen);

        Button goToIngredientView = new Button("Добавить ингредиент (Тест)");
        goToIngredientView.addClickListener(e ->
                UI.getCurrent().navigate("ingredients")
        );
        add(goToIngredientView);

        Button goToPrepackView = new Button("Добавить пфку (Тест)");
        goToPrepackView.addClickListener(e ->
                UI.getCurrent().navigate("prepacks")
        );
        add(goToPrepackView);

        Button goToMenuItemView = new Button("Добавить меню");
        goToMenuItemView.addClickListener(e ->
                UI.getCurrent().navigate("menu-items")
        );
        add(goToMenuItemView);

        Button goToPrepackRecipeView = new Button("Добавить ТТК заготовки");
        goToPrepackRecipeView.addClickListener(e ->
                UI.getCurrent().navigate("prepack-recipe")
        );
        add(goToPrepackRecipeView);

        Button goToMenuItemRecipeView = new Button("Добавить ТТК меню");
        goToMenuItemRecipeView.addClickListener(e ->
                UI.getCurrent().navigate("menu-item-recipe")
        );
        add(goToMenuItemRecipeView);

        Button goToMenuPriceView = new Button("Изменить цены");
        goToMenuPriceView.addClickListener(e ->
                UI.getCurrent().navigate("menu-price")
        );
        add(goToMenuPriceView);
    }
}