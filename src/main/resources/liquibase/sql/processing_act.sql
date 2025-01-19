create table processing_act
(
    amount      double precision                                                                  not null,
    created_at  timestamp with time zone default now()                                            not null,
    date        timestamp                default now()                                            not null,
    employee_id bigint,
    id          bigint                   default nextval('processing_act_id_generator'::regclass) not null
        primary key,
    prepack_id  bigint                                                                            not null
        constraint fk_processing_act_prepack_id_ingredient_id
            references prepack,
    updated_at  timestamp with time zone default now()                                            not null,
    created_by  varchar(255),
    name        varchar(255),
    updated_by  varchar(255)
);

alter table processing_act
    owner to root;

