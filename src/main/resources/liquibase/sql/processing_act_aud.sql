create table processing_act_aud
(
    amount      double precision,
    rev         integer  not null
        constraint fk_processing_act_aud_rev_item_id
            references revinfo,
    revtype     smallint not null,
    created_at  timestamp with time zone default now(),
    date        timestamp                default now(),
    employee_id bigint,
    id          bigint   not null,
    prepack_id  bigint,
    updated_at  timestamp with time zone default now(),
    created_by  varchar(255),
    name        varchar(255),
    updated_by  varchar(255),
    primary key (rev, id)
);

alter table processing_act_aud
    owner to root;

