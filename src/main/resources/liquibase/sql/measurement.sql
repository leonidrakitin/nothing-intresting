create table measurement
(
    id   bigint default nextval('measurement_id_generator'::regclass) not null
        primary key,
    name varchar(255)                                                 not null
);

alter table measurement
    owner to root;

INSERT INTO public.measurement (id, name) VALUES (1, 'г');
INSERT INTO public.measurement (id, name) VALUES (2, 'шт');
