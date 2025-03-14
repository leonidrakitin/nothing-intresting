package ru.sushi.delivery.kds;

import com.vaadin.flow.component.UI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

@EnableScheduling
@SpringBootApplication
public class KdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KdsApplication.class, args);
		UI.getCurrent().setLocale(Locale.forLanguageTag("ru-RU"));
	}

}
