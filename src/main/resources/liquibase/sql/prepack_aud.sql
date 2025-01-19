create table prepack_aud
(
    expiration_duration numeric(21),
    notify_after_amount double precision,
    rev                 integer not null
        constraint fk_prepack_aud_rev_processing_act_id
            references revinfo,
    revtype             smallint,
    created_at          timestamp with time zone default now(),
    id                  bigint  not null,
    measurement_unit_id bigint,
    updated_at          timestamp with time zone default now(),
    created_by          varchar(255),
    name                varchar(255),
    updated_by          varchar(255),
    primary key (rev, id)
);

alter table prepack_aud
    owner to root;

INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, 402, 1, '2025-01-15 18:55:17.166540 +00:00', 9, 1, '2025-01-19 11:13:48.744492 +00:00', null, 'Шапка Жульен', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, 403, 1, '2025-01-15 18:56:52.613582 +00:00', 12, 1, '2025-01-19 11:14:14.293797 +00:00', null, 'Шапка Лагуна', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, 404, 1, '2025-01-16 15:28:55.410652 +00:00', 15, 1, '2025-01-19 11:14:36.352127 +00:00', null, 'Лосось', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (172800000000000, 1000, 405, 1, '2025-01-16 15:30:21.206305 +00:00', 18, 1, '2025-01-19 11:14:50.381707 +00:00', null, 'Тунец', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 500, 406, 1, '2025-01-16 15:29:43.290300 +00:00', 16, 1, '2025-01-19 11:24:20.090584 +00:00', null, 'Креветки вареные ', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 1000, 407, 1, '2025-01-16 15:31:39.839473 +00:00', 20, 1, '2025-01-19 11:26:44.054928 +00:00', null, 'Огурец', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 1000, 408, 1, '2025-01-16 15:31:58.454276 +00:00', 21, 1, '2025-01-19 11:26:53.986594 +00:00', null, 'Авокадо', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 200, 409, 1, '2025-01-16 15:32:36.259244 +00:00', 22, 1, '2025-01-19 11:27:17.027142 +00:00', null, 'Паприка', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (86400000000000, 200, 410, 1, '2025-01-16 15:32:51.346100 +00:00', 23, 1, '2025-01-19 11:27:39.485197 +00:00', null, 'Морковь', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (432000000000000, 500, 411, 1, '2025-01-15 18:46:54.274522 +00:00', 1, 1, '2025-01-19 11:29:00.284134 +00:00', null, 'Соус Спайси', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (259200000000000, 2000, 412, 0, '2025-01-19 11:30:33.122228 +00:00', 25, 1, '2025-01-19 11:30:33.122228 +00:00', null, 'Сливочный сыр ', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 30, 413, 1, '2025-01-16 15:31:03.704666 +00:00', 19, 2, '2025-01-19 11:33:03.849957 +00:00', null, 'Мидии зам п/ф ', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (43200000000000, 15, 414, 0, '2025-01-19 11:34:02.541946 +00:00', 26, 2, '2025-01-19 11:34:02.541946 +00:00', null, 'Мидии деф. п/ф ', null);
INSERT INTO public.prepack_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, updated_by) VALUES (2592000000000000, 30, 415, 0, '2025-01-19 11:34:28.780158 +00:00', 27, 2, '2025-01-19 11:34:28.780158 +00:00', null, 'Спринг-ролл', null);
