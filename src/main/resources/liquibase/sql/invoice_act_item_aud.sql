create table invoice_act_item_aud
(
    amount      double precision,
    price       double precision,
    rev         integer  not null
        constraint fk_invoice_act_item_aud_rev_revinfo_rev
            references revinfo,
    revtype     smallint not null,
    id          bigint   not null,
    invoice_id  bigint,
    source_id   bigint,
    source_type varchar(255),
    primary key (rev, id)
);

alter table invoice_act_item_aud
    owner to root;

