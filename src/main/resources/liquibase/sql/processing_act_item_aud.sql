create table processing_act_item_aud
(
    init_amount       double precision,
    final_amount      double precision,
    losses_amount     double precision,
    losses_percentage double precision,
    rev               integer  not null
        constraint fk_processing_act_item_aud_rev_revinfo_rev
            references revinfo,
    revtype           smallint not null,
    id                bigint   not null,
    processing_id     bigint   not null,
    source_id         bigint,
    source_type       varchar(255),
    primary key (rev, id)
);

alter table processing_act_item_aud
    owner to root;

