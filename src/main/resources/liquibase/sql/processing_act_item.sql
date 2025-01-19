create table processing_act_item
(
    final_amount      double precision                                                     not null,
    init_amount       double precision                                                     not null,
    losses_amount     double precision                                                     not null,
    losses_percentage double precision                                                     not null,
    id                bigint default nextval('processing_act_item_id_generator'::regclass) not null
        primary key,
    processing_id     bigint                                                               not null
        constraint fk_processing_act_item_processing_id_prepack_id
            references processing_act,
    source_id         bigint                                                               not null,
    source_type       varchar(255)                                                         not null
);

alter table processing_act_item
    owner to root;

