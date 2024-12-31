package ru.sushi.delivery.kds;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.service.ChefScreenService;

@Route("")
public class MainView extends VerticalLayout {

    private final ChefScreenService chefScreenService;

    public MainView(@Autowired ChefScreenService chefScreenService) {
        this.chefScreenService = chefScreenService;

        setSizeFull();

        Button createScreenButton = new Button("Создать экран повара");
        Div resultDiv = new Div(); // Здесь будем выводить ссылку

        createScreenButton.addClickListener(e -> {
            newDisplay(resultDiv, 1);
            newDisplay(resultDiv, 2);
            newDisplay(resultDiv, 3);
            int counter = 5;

            Long screenId = chefScreenService.createNewScreen(counter+new Double(Math.random()*100+1).intValue());
            // Сформировать ссылку вида http://localhost:8080/screen/<uuid>
            String link = getUI().get().getInternals().getActiveViewLocation().getPath()
                + "screen/" + screenId.toString();
            // Вывести ссылку на экран
            //            resultDiv.removeAll();
            resultDiv.add(new Div("Экран создан: "));
            Anchor anchor = new Anchor(link, link);
            anchor.setTarget("_blank"); // открыть в новой вкладке
            resultDiv.add(anchor);
        });

        Button goToCreateScreen = new Button("Создать новый заказ");
        goToCreateScreen.addClickListener(e ->
            UI.getCurrent().navigate("create")
        );


        add(createScreenButton, goToCreateScreen, resultDiv);
    }

    private void newDisplay(Div resultDiv, int newId) {
        Long screenId = chefScreenService.createNewScreen(newId);
        // Сформировать ссылку вида http://localhost:8080/screen/<uuid>
        String link = getUI().get().getInternals().getActiveViewLocation().getPath()
            + "screen/" + screenId.toString();
        resultDiv.add(new Div("Экран создан: "));
        Anchor anchor = new Anchor(link, link);
        anchor.setTarget("_blank"); // открыть в новой вкладке
        resultDiv.add(anchor);
    }
}
