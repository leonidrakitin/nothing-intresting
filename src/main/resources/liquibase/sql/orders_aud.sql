create table orders_aud
(
    rev              integer                                not null
        constraint fk_orders_aud_rev_item_id
            references revinfo,
    revtype          smallint                               not null,
    created_at       timestamp with time zone default now() not null,
    id               bigint                                 not null,
    status_update_at timestamp with time zone default now() not null,
    name             varchar(255)                           not null,
    status           varchar(255)                           not null,
    primary key (rev, id)
);

alter table orders_aud
    owner to root;

INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (3, 0, '2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (4, 1, '2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (5, 1, '2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (6, 1, '2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (8, 1, '2025-01-18 11:55:18.671050 +00:00', 1, '2025-01-18 11:55:18.671040 +00:00', '1', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (9, 0, '2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (10, 1, '2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (11, 1, '2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (123, 0, '2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (124, 1, '2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (127, 1, '2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (128, 1, '2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (139, 1, '2025-01-18 12:01:01.274038 +00:00', 2, '2025-01-18 12:01:01.274037 +00:00', 'test', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (155, 1, '2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (156, 1, '2025-01-18 12:57:05.556577 +00:00', 3, '2025-01-18 12:57:05.556573 +00:00', '250118-6792002', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (178, 0, '2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (179, 1, '2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (272, 1, '2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (296, 1, '2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (298, 0, '2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (299, 1, '2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (300, 1, '2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (357, 1, '2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (493, 0, '2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (494, 1, '2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (495, 1, '2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (507, 1, '2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (508, 0, '2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (509, 1, '2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (510, 1, '2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (514, 0, '2025-01-19 13:46:15.223641 +00:00', 8, '2025-01-19 13:46:15.223640 +00:00', '250119-0524848', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (515, 1, '2025-01-19 13:46:15.223641 +00:00', 8, '2025-01-19 13:46:15.223640 +00:00', '250119-0524848', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (518, 1, '2025-01-19 13:46:15.223641 +00:00', 8, '2025-01-19 13:46:15.223640 +00:00', '250119-0524848', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (522, 1, '2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'COLLECTING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (526, 1, '2025-01-19 13:41:12.015465 +00:00', 7, '2025-01-19 13:41:12.015464 +00:00', 'staff', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (527, 1, '2025-01-19 13:24:52.630468 +00:00', 6, '2025-01-19 13:24:52.630459 +00:00', '250119-2641569', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (528, 1, '2025-01-18 14:21:29.252712 +00:00', 5, '2025-01-18 14:21:29.252710 +00:00', '250118-2220482', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (529, 1, '2025-01-18 13:36:48.838398 +00:00', 4, '2025-01-18 13:36:48.838395 +00:00', 'test', 'READY');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (537, 0, '2025-01-19 14:12:13.738247 +00:00', 9, '2025-01-19 14:12:13.738246 +00:00', 'staff', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (538, 1, '2025-01-19 14:12:13.738247 +00:00', 9, '2025-01-19 14:12:13.738246 +00:00', 'staff', 'CREATED');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (539, 1, '2025-01-19 14:12:13.738247 +00:00', 9, '2025-01-19 14:12:13.738246 +00:00', 'staff', 'COOKING');
INSERT INTO public.orders_aud (rev, revtype, created_at, id, status_update_at, name, status) VALUES (540, 1, '2025-01-19 13:46:15.223641 +00:00', 8, '2025-01-19 13:46:15.223640 +00:00', '250119-0524848', 'READY');
