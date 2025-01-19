create table ingredient_item_aud
(
    amount               double precision,
    discontinued_reason  varchar(255),
    rev                  integer  not null
        constraint fk_ingredient_item_aud_rev_revinfo_rev
            references revinfo,
    revtype              smallint not null,
    source_type          varchar(255),
    barcode              bigint,
    created_at           timestamp with time zone default now(),
    discontinued_at      timestamp with time zone default now(),
    expiration_date      timestamp with time zone default now(),
    id                   bigint   not null,
    ingredient_id        bigint,
    invoice_act_item_id  bigint,
    updated_at           timestamp with time zone default now(),
    created_by           varchar(255),
    discontinued_comment varchar(255),
    updated_by           varchar(255),
    primary key (rev, id)
);

alter table ingredient_item_aud
    owner to root;

