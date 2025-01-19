create table ingredient
(
    expiration_duration numeric(21),
    notify_after_amount double precision                                                              not null,
    created_at          timestamp with time zone default now()                                        not null,
    id                  bigint                   default nextval('ingredient_id_generator'::regclass) not null
        primary key,
    measurement_unit_id bigint                                                                        not null
        constraint fk_ingredient_measurement_unit_id_revinfo_rev
            references measurement,
    piece_in_grams      bigint,
    updated_at          timestamp with time zone default now()                                        not null,
    created_by          varchar(255),
    name                varchar(255)                                                                  not null,
    updated_by          varchar(255)
);

alter table ingredient
    owner to root;

INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.837816 +00:00', 2, 1, null, '2025-01-15 18:57:03.837816 +00:00', null, 'Шампиньоны', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.863838 +00:00', 3, 1, null, '2025-01-15 18:57:03.863838 +00:00', null, 'Авокадо', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.886297 +00:00', 4, 1, null, '2025-01-15 18:57:03.886297 +00:00', null, 'Манго', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.908652 +00:00', 5, 1, null, '2025-01-15 18:57:03.908652 +00:00', null, 'Морковь', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.933217 +00:00', 6, 1, null, '2025-01-15 18:57:03.933217 +00:00', null, 'Паприка', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.976569 +00:00', 8, 1, null, '2025-01-15 18:57:03.976569 +00:00', null, 'Чеснок', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.026279 +00:00', 9, 1, null, '2025-01-15 18:57:04.026279 +00:00', null, 'Капуста пекинская', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.070666 +00:00', 10, 1, null, '2025-01-15 18:57:04.070666 +00:00', null, 'Корень имбиря', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.095459 +00:00', 11, 1, null, '2025-01-15 18:57:04.095459 +00:00', null, 'Рис', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.134716 +00:00', 13, 1, null, '2025-01-15 18:57:04.134716 +00:00', null, 'Сыр моцарелла', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.156061 +00:00', 14, 1, null, '2025-01-15 18:57:04.156061 +00:00', null, 'Сыр твердый', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.178780 +00:00', 15, 1, null, '2025-01-15 18:57:04.178780 +00:00', null, 'Майонез', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.195608 +00:00', 16, 1, null, '2025-01-15 18:57:04.195608 +00:00', null, 'Арахисовая паста', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.263791 +00:00', 19, 1, null, '2025-01-15 18:57:04.263791 +00:00', null, 'Кунжутная паста', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.322241 +00:00', 22, 1, null, '2025-01-15 18:57:04.322241 +00:00', null, 'Устричный соус', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.383763 +00:00', 25, 1, null, '2025-01-15 18:57:04.383763 +00:00', null, 'Мисо паста светлая', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.404980 +00:00', 26, 1, null, '2025-01-15 18:57:04.404980 +00:00', null, 'Сгущенка', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.435324 +00:00', 27, 1, null, '2025-01-15 18:57:04.435324 +00:00', null, 'Лосось', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.455522 +00:00', 28, 1, null, '2025-01-15 18:57:04.455522 +00:00', null, 'Тунец', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.492771 +00:00', 29, 1, null, '2025-01-15 18:57:04.492771 +00:00', null, 'Угорь', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.517962 +00:00', 30, 1, null, '2025-01-15 18:57:04.517962 +00:00', null, 'Креветки', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.537128 +00:00', 31, 1, null, '2025-01-15 18:57:04.537128 +00:00', null, 'Красная икра', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.559251 +00:00', 32, 1, null, '2025-01-15 18:57:04.559251 +00:00', null, 'Снежный краб', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.580360 +00:00', 33, 1, null, '2025-01-15 18:57:04.580360 +00:00', null, 'Икра масаго', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.594868 +00:00', 34, 1, null, '2025-01-15 18:57:04.594868 +00:00', null, 'Стружка тунца', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.611871 +00:00', 35, 1, null, '2025-01-15 18:57:04.611871 +00:00', null, 'Мидии', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 40, '2025-01-19 14:34:14.857510 +00:00', 63, 2, null, '2025-01-19 14:34:14.857510 +00:00', null, 'Тесто для спринг-ролла', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.734162 +00:00', 41, 1, null, '2025-01-15 18:57:04.734162 +00:00', null, 'Шиитаке сушеные', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.750252 +00:00', 42, 1, null, '2025-01-15 18:57:04.750252 +00:00', null, 'Чеснок сушеный', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.768789 +00:00', 43, 1, null, '2025-01-15 18:57:04.768789 +00:00', null, 'Лук сушеный', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.783847 +00:00', 44, 1, null, '2025-01-15 18:57:04.783847 +00:00', null, 'Хондаши', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.800474 +00:00', 45, 1, null, '2025-01-15 18:57:04.800474 +00:00', null, 'Кунжут', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.830785 +00:00', 46, 2, null, '2025-01-15 18:57:04.830785 +00:00', null, 'Нори', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.960824 +00:00', 53, 2, null, '2025-01-15 18:57:04.960824 +00:00', null, 'Рисовая бумага', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.978897 +00:00', 54, 1, null, '2025-01-15 18:57:04.978897 +00:00', null, 'Лимонная кислота', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.994781 +00:00', 55, 1, null, '2025-01-15 18:57:04.994781 +00:00', null, 'Мука', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.012412 +00:00', 56, 1, null, '2025-01-15 18:57:05.012412 +00:00', null, 'Панировочные сухари', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.032893 +00:00', 57, 2, null, '2025-01-15 18:57:05.032893 +00:00', null, 'Яйцо куриное', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.051211 +00:00', 58, 1, null, '2025-01-15 18:57:05.051211 +00:00', null, 'Вода', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.081276 +00:00', 60, 1, null, '2025-01-15 18:57:05.081276 +00:00', null, 'Бекон', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.098670 +00:00', 61, 1, null, '2025-01-15 18:57:05.098670 +00:00', null, 'Картофель фри', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.240907 +00:00', 18, 1, null, '2025-01-15 18:57:04.240907 +00:00', null, 'Уксус Рисовый ', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.217344 +00:00', 17, 1, null, '2025-01-15 18:57:04.217344 +00:00', null, 'Соус соевый', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.671188 +00:00', 38, 1, null, '2025-01-15 18:57:04.671188 +00:00', null, 'Паста Карри', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.884469 +00:00', 49, 1, null, '2025-01-15 18:57:04.884469 +00:00', null, 'Сахарная пудра', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.651484 +00:00', 37, 1, null, '2025-01-15 18:57:04.651484 +00:00', null, 'Комбу', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.954096 +00:00', 7, 1, null, '2025-01-15 18:57:03.954096 +00:00', null, 'Лук зеленый', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.281417 +00:00', 20, 1, null, '2025-01-15 18:57:04.281417 +00:00', null, 'Кимчи соус', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.847886 +00:00', 47, 1, null, '2025-01-15 18:57:04.847886 +00:00', null, 'Соль', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.362275 +00:00', 24, 1, null, '2025-01-15 18:57:04.362275 +00:00', null, 'Соус Унаги', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.690088 +00:00', 39, 1, null, '2025-01-15 18:57:04.690088 +00:00', null, 'Васаби порошок', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.712790 +00:00', 40, 1, null, '2025-01-15 18:57:04.712790 +00:00', null, 'Паста Том Ям', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.115884 +00:00', 12, 1, null, '2025-01-15 18:57:04.115884 +00:00', null, 'Сыр сливочный', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.340859 +00:00', 23, 1, null, '2025-01-15 18:57:04.340859 +00:00', null, 'Соус рисовый', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.301126 +00:00', 21, 1, null, '2025-01-15 18:57:04.301126 +00:00', null, 'Масло кунжутное', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.920645 +00:00', 51, 1, null, '2025-01-15 18:57:04.920645 +00:00', null, 'Глутамат Натрия', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.903375 +00:00', 50, 1, null, '2025-01-15 18:57:04.903375 +00:00', null, 'Соль', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.867835 +00:00', 48, 1, null, '2025-01-15 18:57:04.867835 +00:00', null, 'Сахарный песок', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.938828 +00:00', 52, 1, null, '2025-01-15 18:57:04.938828 +00:00', null, 'Крахмал Кукурузный', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:03.805574 +00:00', 1, 1, null, '2025-01-15 18:57:03.805574 +00:00', null, 'Огурцы', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:05.064950 +00:00', 59, 1, null, '2025-01-15 18:57:05.064950 +00:00', null, 'Курица в/к', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (null, 1, '2025-01-15 18:57:04.629463 +00:00', 36, 1, null, '2025-01-15 18:57:04.629463 +00:00', null, 'Перец Сычуанский', null);
INSERT INTO public.ingredient (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, piece_in_grams, updated_at, created_by, name, updated_by) VALUES (63072000000000000, 1, '2025-01-19 12:24:24.613126 +00:00', 62, 2, 13, '2025-01-19 12:24:24.613126 +00:00', null, 'Краситель пищевой оранжевый', null);
