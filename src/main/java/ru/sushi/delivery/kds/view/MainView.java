package ru.sushi.delivery.kds;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    @Autowired
    public MainView(ViewService viewService) {

        setSizeFull();

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
    }
}