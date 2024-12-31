package ru.sushi.delivery.kds.enums;


public enum MenuItem {
    FILADELFIA("Филадельфия"),
    KALIFORNIA("Калифорния"),
    SPICY_ROLL("Спайси ролл"),
    TEMPURA("Темпура"),
    DRAGON("Дракон");

    private final String displayName;

    MenuItem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Переопределим toString, чтобы ComboBox мог красиво отображать
    @Override
    public String toString() {
        return displayName;
    }
}

