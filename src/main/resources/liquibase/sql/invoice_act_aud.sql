create table invoice_act_aud
(
    rev         integer      not null
        constraint fk_invoice_act_aud_rev_measurement_id
            references revinfo,
    revtype     smallint     not null,
    created_at  timestamp with time zone default now(),
    date        timestamp                default now(),
    employee_id bigint,
    id          bigint       not null,
    updated_at  timestamp with time zone default now(),
    created_by  varchar(255),
    vendor      varchar(255) not null,
    name        varchar(255),
    updated_by  varchar(255),
    primary key (rev, id)
);

alter table invoice_act_aud
    owner to root;

