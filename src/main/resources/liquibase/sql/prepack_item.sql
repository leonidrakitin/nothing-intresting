create table prepack_item
(
    amount               double precision                                                                not null,
    discontinued_reason  varchar(255),
    source_type          varchar(255)                                                                    not null,
    barcode              bigint,
    created_at           timestamp with time zone default now()                                          not null,
    discontinued_at      timestamp with time zone default now(),
    expiration_date      timestamp with time zone default now(),
    id                   bigint                   default nextval('prepack_item_id_generator'::regclass) not null
        primary key,
    prepack_id           bigint                                                                          not null
        constraint fk_prepack_item_prepack_id_revinfo_rev
            references prepack,
    processing_act_id    bigint,
    invoice_act_item_id  bigint,
    updated_at           timestamp with time zone default now()                                          not null,
    created_by           varchar(255),
    discontinued_comment varchar(255),
    updated_by           varchar(255),
    measurement_id       bigint
        constraint fk_prepack_menu_item
            references measurement
);

alter table prepack_item
    owner to root;

