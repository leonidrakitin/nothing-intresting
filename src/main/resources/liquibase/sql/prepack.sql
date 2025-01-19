create table prepack
(
    expiration_duration numeric(21),
    notify_after_amount double precision,
    created_at          timestamp with time zone default now()                                     not null,
    id                  bigint                   default nextval('prepack_id_generator'::regclass) not null
        primary key,
    measurement_unit_id bigint                                                                     not null
        constraint fk_prepack_measurement_unit_id_revinfo_rev
            references measurement,
    updated_at          timestamp with time zone default now()                                     not null,
    created_by          varchar(255),
    name                varchar(255)                                                               not null,
    updated_by          varchar(255)
);

alter table prepack
    owner to root;

INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (432000000000000, 500, '2025-01-15 18:47:52.457334 +00:00', 2, 1, '2025-01-15 18:47:52.457334 +00:00', null, 'Кимпаб соус ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-15 18:49:44.418227 +00:00', 3, 1, '2025-01-15 18:49:44.418227 +00:00', null, 'Краб крем', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (1209600000000000, 3000, '2025-01-15 18:50:48.664910 +00:00', 4, 1, '2025-01-15 18:50:48.664910 +00:00', null, 'Соус Сушицу', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 200, '2025-01-15 18:51:19.937806 +00:00', 5, 1, '2025-01-15 18:51:19.937806 +00:00', null, 'Манго соус', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 100, '2025-01-15 18:51:51.078200 +00:00', 6, 1, '2025-01-15 18:51:51.078200 +00:00', null, 'Специи фри', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (259200000000000, 50, '2025-01-15 18:53:36.135419 +00:00', 7, 2, '2025-01-15 18:53:36.135419 +00:00', null, 'Соевый соус доставка', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (259200000000000, 50, '2025-01-15 18:54:03.755443 +00:00', 8, 2, '2025-01-15 18:54:03.755443 +00:00', null, 'Васаби на доставку ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1500, '2025-01-15 18:55:39.399888 +00:00', 10, 1, '2025-01-15 18:55:39.399888 +00:00', null, 'Шапка Лава ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 500, '2025-01-15 18:56:20.779146 +00:00', 11, 1, '2025-01-15 18:56:20.779146 +00:00', null, 'Шапка Моцарелла', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 500, '2025-01-16 15:27:04.950763 +00:00', 13, 1, '2025-01-16 15:27:04.950763 +00:00', null, 'Шапка запеченная креветка', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (43200000000000, 3000, '2025-01-16 15:28:32.350422 +00:00', 14, 1, '2025-01-16 15:28:32.350422 +00:00', null, 'Рис', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-16 15:30:03.856319 +00:00', 17, 1, '2025-01-16 15:30:03.856319 +00:00', null, 'Угорь', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 900, '2025-01-16 15:33:37.732508 +00:00', 24, 1, '2025-01-16 15:33:37.732508 +00:00', null, 'Начинка спринг-ролл', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-15 18:55:17.166540 +00:00', 9, 1, '2025-01-19 11:13:48.744492 +00:00', null, 'Шапка Жульен', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-15 18:56:52.613582 +00:00', 12, 1, '2025-01-19 11:14:14.293797 +00:00', null, 'Шапка Лагуна', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-16 15:28:55.410652 +00:00', 15, 1, '2025-01-19 11:14:36.352127 +00:00', null, 'Лосось', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, '2025-01-16 15:30:21.206305 +00:00', 18, 1, '2025-01-19 11:14:50.381707 +00:00', null, 'Тунец', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 500, '2025-01-16 15:29:43.290300 +00:00', 16, 1, '2025-01-19 11:24:20.090584 +00:00', null, 'Креветки вареные ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 1000, '2025-01-16 15:31:39.839473 +00:00', 20, 1, '2025-01-19 11:26:44.054928 +00:00', null, 'Огурец', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 1000, '2025-01-16 15:31:58.454276 +00:00', 21, 1, '2025-01-19 11:26:53.986594 +00:00', null, 'Авокадо', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 200, '2025-01-16 15:32:36.259244 +00:00', 22, 1, '2025-01-19 11:27:17.027142 +00:00', null, 'Паприка', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 200, '2025-01-16 15:32:51.346100 +00:00', 23, 1, '2025-01-19 11:27:39.485197 +00:00', null, 'Морковь', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (432000000000000, 500, '2025-01-15 18:46:54.274522 +00:00', 1, 1, '2025-01-19 11:29:00.284134 +00:00', null, 'Соус Спайси', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (259200000000000, 2000, '2025-01-19 11:30:33.122228 +00:00', 25, 1, '2025-01-19 11:30:33.122228 +00:00', null, 'Сливочный сыр ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 30, '2025-01-16 15:31:03.704666 +00:00', 19, 2, '2025-01-19 11:33:03.849957 +00:00', null, 'Мидии зам п/ф ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (43200000000000, 15, '2025-01-19 11:34:02.541946 +00:00', 26, 2, '2025-01-19 11:34:02.541946 +00:00', null, 'Мидии деф. п/ф ', null);
INSERT INTO public.prepack (expiration_duration, notify_after_amount, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 30, '2025-01-19 11:34:28.780158 +00:00', 27, 2, '2025-01-19 11:34:28.780158 +00:00', null, 'Спринг-ролл', null);
