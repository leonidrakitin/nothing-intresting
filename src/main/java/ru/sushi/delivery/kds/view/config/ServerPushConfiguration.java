package ru.sushi.delivery.kds.view.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.stereotype.Component;

@Push
@Component
public class ServerPushConfiguration implements AppShellConfigurator {}

