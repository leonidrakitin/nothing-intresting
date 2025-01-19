create table screen
(
    id         bigint default nextval('screen_id_generator'::regclass) not null
        primary key,
    station_id bigint                                                  not null
        constraint fk_screen_station_id_revinfo_rev
            references station
);

alter table screen
    owner to root;

INSERT INTO public.screen (id, station_id) VALUES (1, 2);
INSERT INTO public.screen (id, station_id) VALUES (2, 3);
INSERT INTO public.screen (id, station_id) VALUES (3, 4);
