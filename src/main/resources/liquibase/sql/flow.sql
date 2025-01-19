create table flow
(
    id   bigint default nextval('flow_id_generator'::regclass) not null
        primary key,
    name varchar(255)                                          not null
);

alter table flow
    owner to root;

INSERT INTO public.flow (id, name) VALUES (1, 'Только холодная станция');
INSERT INTO public.flow (id, name) VALUES (2, 'Обе станции');
INSERT INTO public.flow (id, name) VALUES (3, 'Только горячая станция');
