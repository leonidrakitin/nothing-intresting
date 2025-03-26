package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import ru.sushi.delivery.kds.domain.controller.dto.request.GetInvoicesRequest;
import ru.sushi.delivery.kds.domain.controller.dto.response.GetInvoicesResponse;
import ru.sushi.delivery.kds.domain.service.ActService;

import java.util.List;

@Route("invoice-add")
public class InvoiceListView extends VerticalLayout {

    private final ActService actService;
    private final Grid<GetInvoicesResponse> grid;
    private final Dialog deleteDialog;

    public InvoiceListView(ActService actService) {
        this.actService = actService;

        setSizeFull();
        setPadding(true);

        add(new H2("Все накладные"));

        // Инициализация таблицы
        grid = new Grid<>(GetInvoicesResponse.class, false);
        grid.addColumn(GetInvoicesResponse::getDate).setHeader("Дата").setSortable(true);
        grid.addColumn(GetInvoicesResponse::getVendor).setHeader("Поставщик").setSortable(true);
        grid.addColumn(GetInvoicesResponse::getTotalItems).setHeader("Количество")
            .setTextAlign(ColumnTextAlign.END).setSortable(true);
        grid.addColumn(GetInvoicesResponse::getTotalCost).setHeader("Цена")
            .setTextAlign(ColumnTextAlign.END).setSortable(true);
        grid.addColumn(new ComponentRenderer<>(invoice -> new Button("Удалить", e -> confirmDelete(invoice))))
            .setHeader("Действия").setWidth("100px");
        grid.setHeight("70vh");
        grid.addItemClickListener(event -> UI.getCurrent().navigate("invoice-add/" + event.getItem().getId()));

        // Инициализация диалога удаления
        deleteDialog = createDeleteDialog();

        // Кнопка добавления
        Button addButton = new Button("Добавить накладную", e -> UI.getCurrent().navigate("invoice-add/new"));
        HorizontalLayout buttonLayout = new HorizontalLayout(addButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setWidthFull();

        add(grid, buttonLayout);
        refreshGrid();
    }

    private void refreshGrid() {
        try {
            List<GetInvoicesResponse> invoices = actService.getAllInvoices(
                new GetInvoicesRequest(null, null)
            );
            grid.setItems(invoices);
        }
        catch (Exception e) {
            Notification.show("Ошибка загрузки накладных: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void confirmDelete(GetInvoicesResponse invoice) {
        deleteDialog.removeAll();
        deleteDialog.add("Вы уверены, что хотите удалить накладную от " + invoice.getDate() + "?");
        deleteDialog.getFooter().removeAll();
        deleteDialog.getFooter().add(
            new Button("Отмена", e -> deleteDialog.close()),
            new Button("Удалить", e -> {
                try {
                    actService.deleteInvoiceAct(invoice.getId());
                    refreshGrid();
                    Notification.show("Накладная удалена", 2000, Notification.Position.TOP_CENTER);
                }
                catch (Exception ex) {
                    Notification.show("Ошибка удаления: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                }
                deleteDialog.close();
            })
        );
        deleteDialog.open();
    }

    private Dialog createDeleteDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Подтверждение удаления");
        dialog.setModal(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        return dialog;
    }
}