create table station
(
    id                      bigint default nextval('station_id_generator'::regclass) not null
        primary key,
    name                    varchar(255)                                             not null,
    order_status_at_station varchar(255)                                             not null
);

alter table station
    owner to root;

INSERT INTO public.station (id, name, order_status_at_station) VALUES (1, 'Не задан', 'CREATED');
INSERT INTO public.station (id, name, order_status_at_station) VALUES (2, 'Холодный цех', 'COOKING');
INSERT INTO public.station (id, name, order_status_at_station) VALUES (3, 'Горячий цех', 'COOKING');
INSERT INTO public.station (id, name, order_status_at_station) VALUES (4, 'Сбор заказа', 'COLLECTING');
INSERT INTO public.station (id, name, order_status_at_station) VALUES (5, 'Кассовая зона', 'READY');
INSERT INTO public.station (id, name, order_status_at_station) VALUES (6, 'Отменен', 'CANCELED');
