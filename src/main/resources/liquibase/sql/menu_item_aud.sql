create table menu_item_aud
(
    rev     integer not null
        constraint fk_item_aud_rev_revinfo_rev
            references revinfo,
    revtype smallint,
    flow_id bigint,
    id      bigint  not null,
    name    varchar(255),
    constraint item_aud_pkey
        primary key (rev, id)
);

alter table menu_item_aud
    owner to root;

