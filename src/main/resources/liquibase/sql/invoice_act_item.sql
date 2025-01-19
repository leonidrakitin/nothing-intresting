create table invoice_act_item
(
    amount      double precision                                                  not null,
    price       double precision                                                  not null,
    id          bigint default nextval('invoice_act_item_id_generator'::regclass) not null
        primary key,
    invoice_id  bigint                                                            not null
        constraint fk_invoice_act_item_invoice_id
            references invoice_act,
    source_id   bigint                                                            not null,
    source_type varchar(255)                                                      not null
);

alter table invoice_act_item
    owner to root;

