create table order_item
(
    current_flow_step  integer                                                                        not null,
    id                 bigint                   default nextval('orders_item_id_generator'::regclass) not null
        primary key,
    order_id           bigint                                                                         not null
        constraint fk_order_item_order_id_revinfo_rev
            references orders,
    menu_item_id       bigint                                                                         not null
        constraint fk_order_item_menu_item_id_revinfo_rev
            references menu_item,
    station_changed_at timestamp with time zone default now()                                         not null,
    status_updated_at  timestamp with time zone default now()                                         not null,
    status             varchar(255)                                                                   not null
);

alter table order_item
    owner to root;

INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 1, 1, 1, '2025-01-18 11:55:47.408478 +00:00', '2025-01-18 11:55:47.408443 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 29, 8, 20, '2025-01-19 14:09:02.622260 +00:00', '2025-01-19 14:09:02.622203 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 27, 8, 37, '2025-01-19 13:57:48.932154 +00:00', '2025-01-19 13:57:48.932141 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 26, 8, 8, '2025-01-19 14:00:15.987677 +00:00', '2025-01-19 14:00:15.987618 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 28, 8, 21, '2025-01-19 14:05:42.863584 +00:00', '2025-01-19 14:05:42.863576 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (2, 30, 9, 26, '2025-01-19 14:18:34.808943 +00:00', '2025-01-19 14:18:34.808812 +00:00', 'ADDED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 3, 2, 2, '2025-01-18 12:57:20.926357 +00:00', '2025-01-18 12:57:20.926297 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 2, 2, 2, '2025-01-18 12:57:23.664830 +00:00', '2025-01-18 12:57:23.664820 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 4, 3, 38, '2025-01-18 13:09:13.713199 +00:00', '2025-01-18 13:09:13.713192 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 6, 3, 38, '2025-01-18 13:09:14.728226 +00:00', '2025-01-18 13:09:14.728219 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 7, 3, 41, '2025-01-18 13:09:15.434660 +00:00', '2025-01-18 13:09:15.434649 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 8, 3, 41, '2025-01-18 13:09:16.133684 +00:00', '2025-01-18 13:09:16.133676 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 5, 3, 4, '2025-01-18 13:13:44.723922 +00:00', '2025-01-18 13:13:44.723914 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 25, 7, 30, '2025-01-19 13:57:41.344573 +00:00', '2025-01-19 13:57:41.344562 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 24, 7, 23, '2025-01-19 13:57:42.073213 +00:00', '2025-01-19 13:57:43.382989 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 21, 6, 3, '2025-01-19 13:34:33.186702 +00:00', '2025-01-19 13:34:33.185333 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 23, 6, 10, '2025-01-19 13:39:18.585922 +00:00', '2025-01-19 13:39:18.585913 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 22, 6, 14, '2025-01-19 13:39:18.921510 +00:00', '2025-01-19 13:39:18.921503 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 16, 5, 14, '2025-01-18 14:33:55.944197 +00:00', '2025-01-18 14:33:56.332798 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 17, 5, 15, '2025-01-18 14:33:56.799611 +00:00', '2025-01-18 14:33:56.799604 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 18, 5, 16, '2025-01-18 14:33:56.982157 +00:00', '2025-01-18 14:33:56.982150 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 19, 5, 17, '2025-01-18 14:33:57.147759 +00:00', '2025-01-18 14:33:57.147753 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 20, 5, 18, '2025-01-18 14:33:57.382353 +00:00', '2025-01-18 14:33:57.613835 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 9, 4, 1, '2025-01-18 14:20:30.275319 +00:00', '2025-01-18 14:20:30.429686 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 10, 4, 2, '2025-01-18 14:20:30.674878 +00:00', '2025-01-18 14:20:30.808749 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 12, 4, 7, '2025-01-18 14:20:31.440451 +00:00', '2025-01-18 14:20:31.440442 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 11, 4, 4, '2025-01-18 14:20:34.897766 +00:00', '2025-01-18 14:20:35.023914 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 13, 4, 8, '2025-01-18 14:20:35.355666 +00:00', '2025-01-18 14:20:35.355655 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 14, 4, 13, '2025-01-18 14:20:35.707532 +00:00', '2025-01-18 14:20:35.707524 +00:00', 'COMPLETED');
INSERT INTO public.order_item (current_flow_step, id, order_id, menu_item_id, station_changed_at, status_updated_at, status) VALUES (0, 15, 4, 11, '2025-01-18 14:20:36.037845 +00:00', '2025-01-18 14:20:36.189222 +00:00', 'COMPLETED');
