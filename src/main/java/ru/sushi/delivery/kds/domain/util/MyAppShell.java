package ru.sushi.delivery.kds.domain.util;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.stereotype.Component;

@Push
@Component
public class MyAppShell implements AppShellConfigurator {}

