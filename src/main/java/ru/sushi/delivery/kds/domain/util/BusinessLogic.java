package ru.sushi.delivery.kds.domain.util;

import lombok.experimental.UtilityClass;
import ru.sushi.delivery.kds.domain.model.OrderStatus;
import ru.sushi.delivery.kds.domain.persist.entity.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.ItemSet;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.Station;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ru.sushi.delivery.kds.domain.util.IngredientsCatalog.*;

@UtilityClass
public class BusinessLogic {

    public static final Measurement MEASUREMENT_GRAMS = new Measurement("г");
    public static final Measurement MEASUREMENT_AMOUNT = new Measurement("шт");

    public static final Station STATION_TYPE_DEFAULT = new Station(1L, "NULL", OrderStatus.CREATED);
    public static final Station STATION_TYPE_COLD = new Station(2L, "ХОЛОДНЫЙ ЦЕХ", OrderStatus.COOKING);
    public static final Station STATION_TYPE_HOT = new Station(3L, "ГОРЯЧИЙ ЦЕХ", OrderStatus.COOKING);
    public static final Station STATION_TYPE_COLLECT = new Station(4L, "СБОР ЗАКАЗА", OrderStatus.COLLECTING);

    public static final Screen COLD_SCREEN = new Screen(STATION_TYPE_COLD);
    public static final Screen HOT_SCREEN = new Screen(STATION_TYPE_HOT);
    public static final Screen COLLECT_SCREEN = new Screen(STATION_TYPE_COLLECT);



        // Пример cоусов и шапок
        public static final Ingredient INGREDIENT_SOUS_UNAGI_10       = new Ingredient(1L,  "Соус унаги",   BusinessLogic.MEASUREMENT_GRAMS, 10);
        public static final Ingredient INGREDIENT_SOUS_UNAGI_5        = new Ingredient(2L,  "Соус унаги",   BusinessLogic.MEASUREMENT_GRAMS, 5);
        public static final Ingredient INGREDIENT_SOUS_SPICY_10       = new Ingredient(3L,  "Соус спайси",  BusinessLogic.MEASUREMENT_GRAMS, 10);
        public static final Ingredient INGREDIENT_SOUS_SPICY_5        = new Ingredient(4L,  "Соус спайси",  BusinessLogic.MEASUREMENT_GRAMS, 5);
        public static final Ingredient INGREDIENT_SOUS_SPICY_TOMYAM_40= new Ingredient(5L,  "Соус спайси том ям", BusinessLogic.MEASUREMENT_GRAMS, 40);
        public static final Ingredient INGREDIENT_SOUS_KIMBAB_10      = new Ingredient(6L,  "Кимпаб соус",  BusinessLogic.MEASUREMENT_GRAMS, 10);
        public static final Ingredient INGREDIENT_SOUS_ORXEH_5        = new Ingredient(7L,  "Соус ореховый",BusinessLogic.MEASUREMENT_GRAMS, 5);
        public static final Ingredient INGREDIENT_MISO_MAYO_25        = new Ingredient(8L,  "Мисо-майо",    BusinessLogic.MEASUREMENT_GRAMS, 25);
        public static final Ingredient INGREDIENT_SHAPKA_LAVA_80      = new Ingredient(9L,  "Шапка Лава",   BusinessLogic.MEASUREMENT_GRAMS, 80);
        public static final Ingredient INGREDIENT_SHAPKA_LAVA_120     = new Ingredient(10L, "Шапка Лава",   BusinessLogic.MEASUREMENT_GRAMS, 120);
        public static final Ingredient INGREDIENT_SHAPKA_LAGUNA_80    = new Ingredient(11L, "Шапка Лагуна", BusinessLogic.MEASUREMENT_GRAMS, 80);
        public static final Ingredient INGREDIENT_SHAPKA_GRIBNAYA_120 = new Ingredient(12L, "Шапка грибная",BusinessLogic.MEASUREMENT_GRAMS, 120);
        public static final Ingredient INGREDIENT_SHAP_ZAPECH_KREV_120= new Ingredient(13L, "Шап. Зап. Креветка", BusinessLogic.MEASUREMENT_GRAMS, 120);

        // Пример «начинки»
        public static final Ingredient INGREDIENT_OGUREC_20           = new Ingredient(14L, "Огурец",       BusinessLogic.MEASUREMENT_GRAMS, 20);
        public static final Ingredient INGREDIENT_PAPRIKA_10          = new Ingredient(15L, "Паприка",      BusinessLogic.MEASUREMENT_GRAMS, 10);
        public static final Ingredient INGREDIENT_MORKOV_10           = new Ingredient(16L, "Морковь",      BusinessLogic.MEASUREMENT_GRAMS, 10);
        public static final Ingredient INGREDIENT_AVOCADO_20          = new Ingredient(17L, "Авокадо",      BusinessLogic.MEASUREMENT_GRAMS, 20);

        // Различные рыбы / мясо / морепродукты
        public static final Ingredient INGREDIENT_LOSOS_60            = new Ingredient(18L, "Лосось",       BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_LOSOS_80            = new Ingredient(19L, "Лосось",       BusinessLogic.MEASUREMENT_GRAMS, 80);
        public static final Ingredient INGREDIENT_LOSOS_40            = new Ingredient(20L, "Лосось",       BusinessLogic.MEASUREMENT_GRAMS, 40);
        public static final Ingredient INGREDIENT_LOSOS_30            = new Ingredient(21L, "Лосось",       BusinessLogic.MEASUREMENT_GRAMS, 30); // "лосось внутрь 30г"

        public static final Ingredient INGREDIENT_UGOR_60             = new Ingredient(22L, "Угорь",        BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_UGOR_30             = new Ingredient(23L, "Угорь",        BusinessLogic.MEASUREMENT_GRAMS, 30); // Для маки угорь 30г
        public static final Ingredient INGREDIENT_UGOR_20             = new Ingredient(24L, "Угорь",        BusinessLogic.MEASUREMENT_GRAMS, 20); // Для суши
        public static final Ingredient INGREDIENT_UGOR_15             = new Ingredient(25L, "Угорь",        BusinessLogic.MEASUREMENT_GRAMS, 15); // Гункан

        public static final Ingredient INGREDIENT_CHICKEN_65          = new Ingredient(26L, "Курица в/к",   BusinessLogic.MEASUREMENT_GRAMS, 65);
        public static final Ingredient INGREDIENT_BACON_60            = new Ingredient(27L, "Бекон",        BusinessLogic.MEASUREMENT_GRAMS, 60);

        public static final Ingredient INGREDIENT_KREVETKA_15         = new Ingredient(155L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_KREVETKA_20         = new Ingredient(159L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_KREVETKA_60         = new Ingredient(28L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_KREVETKA_80         = new Ingredient(29L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 80);
        public static final Ingredient INGREDIENT_KREVETKA_120        = new Ingredient(30L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 120);
        public static final Ingredient INGREDIENT_KREVETKA_40         = new Ingredient(31L, "Креветка",     BusinessLogic.MEASUREMENT_GRAMS, 40); // "креветка внутрь"

        public static final Ingredient INGREDIENT_TUNEC_60            = new Ingredient(32L, "Тунец",        BusinessLogic.MEASUREMENT_GRAMS, 60); // внутри
        public static final Ingredient INGREDIENT_TUNEC_30            = new Ingredient(33L, "Тунец",        BusinessLogic.MEASUREMENT_GRAMS, 30); // для маки
        public static final Ingredient INGREDIENT_TUNEC_15            = new Ingredient(34L, "Тунец",        BusinessLogic.MEASUREMENT_GRAMS, 15); // если вдруг потребуется

        // Сыры / крем
        public static final Ingredient INGREDIENT_CREAM_CHEESE_60     = new Ingredient(35L, "Сливочный сыр",BusinessLogic.MEASUREMENT_GRAMS, 60);
        public static final Ingredient INGREDIENT_CREAM_CHEESE_30     = new Ingredient(36L, "Сливочный сыр",BusinessLogic.MEASUREMENT_GRAMS, 30); // если понадобится
        public static final Ingredient INGREDIENT_KRAB_KREM_80        = new Ingredient(37L, "Краб-крем",    BusinessLogic.MEASUREMENT_GRAMS, 80);
        public static final Ingredient INGREDIENT_MOZZARELLA_80       = new Ingredient(38L, "Шапка моцарелла", BusinessLogic.MEASUREMENT_GRAMS, 80);

        // Икра
        public static final Ingredient INGREDIENT_IKRA_MASAGO_40      = new Ingredient(39L, "Икра масаго",  BusinessLogic.MEASUREMENT_GRAMS, 40);
        public static final Ingredient INGREDIENT_IKRA_KRASNAYA_35    = new Ingredient(40L, "Красная икра", BusinessLogic.MEASUREMENT_GRAMS, 35);

        // Кунжут
        public static final Ingredient INGREDIENT_KUNZHUT_20          = new Ingredient(41L, "Кунжут",       BusinessLogic.MEASUREMENT_GRAMS, 20);

        // Манго соус
        public static final Ingredient INGREDIENT_MANGO_SOUS_20       = new Ingredient(42L, "Манго соус",   BusinessLogic.MEASUREMENT_GRAMS, 20);

        // Рис (в разных весах)
        public static final Ingredient INGREDIENT_RICE_150            = new Ingredient(43L, "Рис",          BusinessLogic.MEASUREMENT_GRAMS, 150);
        public static final Ingredient INGREDIENT_RICE_100            = new Ingredient(44L, "Рис",          BusinessLogic.MEASUREMENT_GRAMS, 100);
        public static final Ingredient INGREDIENT_RICE_20             = new Ingredient(45L, "Рис",          BusinessLogic.MEASUREMENT_GRAMS, 20);

        // Нори (в разных кол-вах листов)
        public static final Ingredient INGREDIENT_NORI_1              = new Ingredient(46L, "Нори",         BusinessLogic.MEASUREMENT_AMOUNT, 1);
        public static final Ingredient INGREDIENT_NORI_05             = new Ingredient(47L, "Нори",         BusinessLogic.MEASUREMENT_AMOUNT, 0.5);
        public static final Ingredient INGREDIENT_NORI_075            = new Ingredient(48L, "Нори",         BusinessLogic.MEASUREMENT_AMOUNT, 0.75);
        public static final Ingredient INGREDIENT_NORI_025            = new Ingredient(49L, "Нори",         BusinessLogic.MEASUREMENT_AMOUNT, 0.25);

        public static final List<Item> items = new ArrayList<>(List.of(
            new Item(
                    1L,
                    "Лава с лососем",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_SHAPKA_LAVA_80,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_LOSOS_60,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    2L,
                    "Лава с угрем",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_SPICY_10,
                            INGREDIENT_SHAPKA_LAVA_80,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_UGOR_60,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    3L,
                    "Лава с креветкой",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_SHAPKA_LAVA_80,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_KREVETKA_60,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    4L,
                    "Лава с беконом",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_SPICY_10,
                            INGREDIENT_SHAPKA_LAVA_120,
                            INGREDIENT_BACON_60,
                            INGREDIENT_MORKOV_10,
                            INGREDIENT_PAPRIKA_10,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    5L,
                    "Лава с курицей",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_SHAPKA_LAVA_120,
                            INGREDIENT_CHICKEN_65,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    6L,
                    "Лагуна",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_KIMBAB_10,
                            INGREDIENT_SHAPKA_LAGUNA_80,
                            INGREDIENT_KRAB_KREM_80,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    7L,
                    "Лосось моцарелла",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_MOZZARELLA_80,
                            INGREDIENT_LOSOS_60,
                            INGREDIENT_IKRA_MASAGO_40,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            // внимание на 0.75 нори или 0.5; по рецепту написано 0,75
                            INGREDIENT_NORI_075
                    )
            ),
            new Item(
                    8L,
                    "Жульен",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_SHAPKA_GRIBNAYA_120,
                            INGREDIENT_CHICKEN_65,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    9L,
                    "Запеченная креветка",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_HOT, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_SHAP_ZAPECH_KREV_120,
                            INGREDIENT_KRAB_KREM_80,
                            INGREDIENT_KREVETKA_120,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    10L,
                    "Фила с авакадо",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_LOSOS_80,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    11L,
                    "Фила",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_LOSOS_80,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    12L,
                    "Фила с икрой",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_LOSOS_80,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            // нори 0,75
                            INGREDIENT_NORI_075,
                            // "шапка" из 30г сыра + 35г красной икры,
                            // но в задании указано просто: "шапка: сливочный сыр 30г + красная икра 35г"
                            // при желании можно добавить отдельные ингредиенты
                            INGREDIENT_CREAM_CHEESE_30, // если хотите явно
                            INGREDIENT_IKRA_KRASNAYA_35
                    )
            ),
            new Item(
                    13L,
                    "Фила с креветкой внутрь",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_LOSOS_40,
                            INGREDIENT_KREVETKA_40,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    14L,
                    "Фила с манго соусом",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_MANGO_SOUS_20,
                            INGREDIENT_LOSOS_80,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    15L,
                    "Ролл с тунцом внутрь",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_TUNEC_60, // "тунец внутрь"
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    16L,
                    "Опаленный тунец",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_KIMBAB_10,
                            INGREDIENT_TUNEC_60,
                            INGREDIENT_LOSOS_30,  // "лосось внутрь 30г"
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    17L,
                    "Ролл с угрем",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_UGOR_60,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    18L,
                    "Спайси рубленный тунец",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_KUNZHUT_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05,
                            // Наверх замешивается: соус спайси том ям 40г + лосось 60г
                            INGREDIENT_SOUS_SPICY_TOMYAM_40,
                            INGREDIENT_LOSOS_60
                    )
            ),
            new Item(
                    19L,
                    "Спайси лосось",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_KUNZHUT_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05,
                            // Наверх замешивается: соус спайси том ям 40г + лосось 60г
                            INGREDIENT_SOUS_SPICY_TOMYAM_40,
                            INGREDIENT_LOSOS_60
                    )
            ),
            new Item(
                    20L,
                    "Сливочная креветка",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_10,
                            INGREDIENT_KREVETKA_80,
                            INGREDIENT_CREAM_CHEESE_60,
                            INGREDIENT_AVOCADO_20,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    21L,
                    "Калифорния",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_KRAB_KREM_80,
                            INGREDIENT_LOSOS_30, // "лосось внутрь 30г"
                            INGREDIENT_IKRA_MASAGO_40,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    22L,
                    "Калифорния с креветкой",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_KRAB_KREM_80,
                            INGREDIENT_LOSOS_30,
                            INGREDIENT_KREVETKA_40, // "креветка внутрь 40г"
                            INGREDIENT_IKRA_MASAGO_40,
                            INGREDIENT_OGUREC_20,
                            INGREDIENT_RICE_150,
                            INGREDIENT_NORI_05
                    )
            ),
            new Item(
                    23L,
                    "Маки огурец",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_OGUREC_20, // хотя в списке у вас "30г", возможно опечатка, см. ниже
                            INGREDIENT_RICE_100,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    24L,
                    "Маки креветка",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_KREVETKA_40, // 40г
                            INGREDIENT_RICE_100,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    25L,
                    "Маки лосось",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_LOSOS_30, // 30г
                            INGREDIENT_RICE_100,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    26L,
                    "Маки угорь",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_UGOR_30, // 30г
                            INGREDIENT_RICE_100,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    27L,
                    "Маки тунец",
                    new LinkedList<>(List.of(STATION_TYPE_COLD, STATION_TYPE_COLLECT)),
                    List.of(
                            INGREDIENT_TUNEC_30, // 30г
                            INGREDIENT_RICE_100,
                            INGREDIENT_NORI_1
                    )
            ),
            new Item(
                    28L,
                    "Суши угорь",
                    new LinkedList<>(List.of(STATION_TYPE_COLD)),
                    List.of(
                            INGREDIENT_SOUS_UNAGI_5,
                            INGREDIENT_UGOR_20,
                            INGREDIENT_RICE_20
                    )
            ),
            new Item(
                    29L,
                    "Суши креветка",
                    new LinkedList<>(List.of(STATION_TYPE_COLD)),
                    List.of(
                            INGREDIENT_KREVETKA_20,
                            INGREDIENT_RICE_20
                    )
            ),
            new Item(
                    30L,
                    "Гункан креветка",
                    new LinkedList<>(List.of(STATION_TYPE_COLD)),
                    List.of(
                            INGREDIENT_MISO_MAYO_25,
                            INGREDIENT_KREVETKA_15,
                            INGREDIENT_RICE_20,
                            INGREDIENT_NORI_025
                    )
            ),
            new Item(
                    31L,
                    "Гункан угорь спайси вафу",
                    new LinkedList<>(List.of(STATION_TYPE_COLD)),
                    List.of(
                            INGREDIENT_SOUS_SPICY_5,
                            INGREDIENT_SOUS_ORXEH_5,
                            INGREDIENT_UGOR_15,
                            INGREDIENT_RICE_20,
                            INGREDIENT_NORI_025
                    )
            )
    ));

        public static final List<ItemSet> itemSets = new ArrayList<>(List.of(
            new ItemSet(
                1L,
                "Сет Тест",
                List.of(
                    items.get(0),
                    items.get(1),
                    items.get(2)
                )
            )
        ));

        // Объединённый список со всеми статическими переменными (по желанию)
        public static final List<Ingredient> ALL_INGREDIENTS = List.of(
                INGREDIENT_SOUS_UNAGI_10,
                INGREDIENT_SOUS_UNAGI_5,
                INGREDIENT_SOUS_SPICY_10,
                INGREDIENT_SOUS_SPICY_5,
                INGREDIENT_SOUS_SPICY_TOMYAM_40,
                INGREDIENT_SOUS_KIMBAB_10,
                INGREDIENT_SOUS_ORXEH_5,
                INGREDIENT_MISO_MAYO_25,
                INGREDIENT_SHAPKA_LAVA_80,
                INGREDIENT_SHAPKA_LAVA_120,
                INGREDIENT_SHAPKA_LAGUNA_80,
                INGREDIENT_SHAPKA_GRIBNAYA_120,
                INGREDIENT_SHAP_ZAPECH_KREV_120,
                INGREDIENT_OGUREC_20,
                INGREDIENT_PAPRIKA_10,
                INGREDIENT_MORKOV_10,
                INGREDIENT_AVOCADO_20,
                INGREDIENT_LOSOS_60,
                INGREDIENT_LOSOS_80,
                INGREDIENT_LOSOS_40,
                INGREDIENT_LOSOS_30,
                INGREDIENT_UGOR_60,
                INGREDIENT_UGOR_30,
                INGREDIENT_UGOR_20,
                INGREDIENT_UGOR_15,
                INGREDIENT_CHICKEN_65,
                INGREDIENT_BACON_60,
                INGREDIENT_KREVETKA_60,
                INGREDIENT_KREVETKA_80,
                INGREDIENT_KREVETKA_120,
                INGREDIENT_KREVETKA_40,
                INGREDIENT_TUNEC_60,
                INGREDIENT_TUNEC_30,
                INGREDIENT_TUNEC_15,
                INGREDIENT_CREAM_CHEESE_60,
                INGREDIENT_CREAM_CHEESE_30,
                INGREDIENT_KRAB_KREM_80,
                INGREDIENT_MOZZARELLA_80,
                INGREDIENT_IKRA_MASAGO_40,
                INGREDIENT_IKRA_KRASNAYA_35,
                INGREDIENT_KUNZHUT_20,
                INGREDIENT_MANGO_SOUS_20,
                INGREDIENT_RICE_150,
                INGREDIENT_RICE_100,
                INGREDIENT_RICE_20,
                INGREDIENT_NORI_1,
                INGREDIENT_NORI_05,
                INGREDIENT_NORI_075,
                INGREDIENT_NORI_025
        );

}
