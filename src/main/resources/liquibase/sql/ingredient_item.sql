create table ingredient_item
(
    amount               double precision                                                                   not null,
    discontinued_reason  varchar(255),
    source_type          varchar(255)                                                                       not null,
    barcode              bigint,
    created_at           timestamp with time zone default now()                                             not null,
    discontinued_at      timestamp with time zone default now(),
    expiration_date      timestamp with time zone default now(),
    id                   bigint                   default nextval('ingredient_item_id_generator'::regclass) not null
        primary key,
    ingredient_id        bigint                                                                             not null
        constraint fk_ingredient_item_ingredient_id_station_id
            references ingredient,
    invoice_act_item_id  bigint,
    updated_at           timestamp with time zone default now()                                             not null,
    created_by           varchar(255),
    discontinued_comment varchar(255),
    updated_by           varchar(255)
);

alter table ingredient_item
    owner to root;

