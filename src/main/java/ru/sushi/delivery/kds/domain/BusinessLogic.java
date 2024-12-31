package ru.sushi.delivery.kds.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BusinessLogic {

    public static final Measurement MEASUREMENT_GRAMS = new Measurement(1, "г");
    public static final Measurement MEASUREMENT_AMOUNT = new Measurement(2, "шт");

    public static final PlaceType PLACE_TYPE_DEFAULT = new PlaceType(1, "NULL");
    public static final PlaceType PLACE_TYPE_COLD = new PlaceType(2, "ХОЛОДНЫЙ ЦЕХ");

    public static final PlaceType PLACE_TYPE_HOT = new PlaceType(3, "ГОРЯЧИЙ ЦЕХ");
    public static final PlaceType PLACE_TYPE_COLLECT = new PlaceType(4, "СБОР ЗАКАЗА");

    static {
        PLACE_TYPE_COLD.getDisplays().add(1L);
        PLACE_TYPE_HOT.getDisplays().add(2L);
        PLACE_TYPE_COLLECT.getDisplays().add(3L);
    }

    public static final Ingredient INGREDIENT_RICE_150 = new Ingredient(1, "Рис", MEASUREMENT_GRAMS, 150);
    public static final Ingredient INGREDIENT_RICE_100 = new Ingredient(6, "Рис", MEASUREMENT_GRAMS, 100);
    public static final Ingredient INGREDIENT_NORI_025 = new Ingredient(6, "Нори", MEASUREMENT_AMOUNT, 0.25);
    public static final Ingredient INGREDIENT_NORI_1 = new Ingredient(3, "Нори", MEASUREMENT_AMOUNT, 1);;
    public static final Ingredient INGREDIENT_NORI_075 = new Ingredient(4, "Нори", MEASUREMENT_AMOUNT, 0.75);
    public static final Ingredient INGREDIENT_NORI_05 = new Ingredient(4, "Нори", MEASUREMENT_AMOUNT, 0.5);
    public static final Ingredient INGREDIENT_CHEESE = new Ingredient(5, "Сливочный сыр", MEASUREMENT_GRAMS, 60);


    public static final List<Item> items = new ArrayList<>(List.of(
            new Item(
                    1,
                    "Лава с лососем",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Лава с беконом",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Лава с угрем",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Лава с курицей",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Лагуна",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Жульен",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Лагуна",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Запеченная креветка",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_1, INGREDIENT_RICE_150)
            ),
            //холодные роллы
            new Item(
                    1,
                    "Филадельфия с огурцом",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Филадельфия с авокадо",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Филадельфия с креветкой",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Филадельфия с манго соусом",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Филадельфия с икрой",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Ролл с тунцом",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Опаленный тунец",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Ролл с угрем",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Спайси рубленный тунец",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Спайси лосось",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Сливочная креветка",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Калифорния",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Калифорния с креветкой",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Маки огурец",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Маки креветка",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Маки лосось",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Маки угорь",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Маки тунец",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Нигири угорь",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Нигири креветка",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Гункан креветка",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            ),
            new Item(
                    1,
                    "Гункан угорь спайси вафу",
                    new LinkedList<>(List.of(PLACE_TYPE_COLD, PLACE_TYPE_HOT, PLACE_TYPE_COLLECT)),
                    List.of(INGREDIENT_CHEESE, INGREDIENT_NORI_05, INGREDIENT_RICE_150)
            )
    ));
}
