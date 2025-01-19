create table ingredient_aud
(
    expiration_duration numeric(21),
    notify_after_amount double precision,
    rev                 integer not null
        constraint fk_ingredient_aud_rev_prepack_id
            references revinfo,
    revtype             smallint,
    created_at          timestamp with time zone default now(),
    id                  bigint  not null,
    measurement_unit_id bigint,
    updated_at          timestamp with time zone default now(),
    created_by          varchar(255),
    name                varchar(255),
    piece_in_grams      bigint,
    updated_by          varchar(255),
    primary key (rev, id)
);

alter table ingredient_aud
    owner to root;

INSERT INTO public.ingredient_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, piece_in_grams, updated_by) VALUES (63072000000000000, 1, 437, 0, '2025-01-19 12:24:24.613126 +00:00', 62, 2, '2025-01-19 12:24:24.613126 +00:00', null, 'Краситель пищевой оранжевый', 13, null);
INSERT INTO public.ingredient_aud (expiration_duration, notify_after_amount, rev, revtype, created_at, id, measurement_unit_id, updated_at, created_by, name, piece_in_grams, updated_by) VALUES (2592000000000000, 40, 547, 0, '2025-01-19 14:34:14.857510 +00:00', 63, 2, '2025-01-19 14:34:14.857510 +00:00', null, 'Тесто для спринг-ролла', null, null);
