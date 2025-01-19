create table invoice_act
(
    created_at  timestamp with time zone default now()                                         not null,
    date        timestamp                default now()                                         not null,
    employee_id bigint,
    id          bigint                   default nextval('invoice_act_id_generator'::regclass) not null
        primary key,
    updated_at  timestamp with time zone default now()                                         not null,
    created_by  varchar(255),
    vendor      varchar(255)                                                                   not null,
    name        varchar(255),
    updated_by  varchar(255)
);

alter table invoice_act
    owner to root;

