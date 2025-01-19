create table orders
(
    created_at       timestamp with time zone default now()                                    not null,
    id               bigint                   default nextval('orders_id_generator'::regclass) not null
        primary key,
    status_update_at timestamp with time zone default now()                                    not null,
    name             varchar(255)                                                              not null,
    status           varchar(255)                                                              not null
);

alter table orders
    owner to root;

INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'READY');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-19 14:12:13.738247 +00:00', 9, '2025-01-19 14:12:13.738246 +00:00', 'staff', 'COOKING');
INSERT INTO public.orders (created_at, id, status_update_at, name, status) VALUES ('2025-01-19 13:46:15.223641 +00:00', 8, '2025-01-19 13:46:15.223640 +00:00', '250119-0524848', 'READY');
