create table menu_item
(
    flow_id bigint                                                         not null
        constraint fk_item_flow_id_prepack_id
            references flow,
    id      bigint default nextval('menu_item_id_seq_generator'::regclass) not null
        constraint item_pkey
            primary key,
    name    varchar(255)                                                   not null
);

alter table menu_item
    owner to root;

INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 1, 'Филадельфия');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 2, 'Филадельфия с авокадо');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 3, 'Филадельфия с креветкой');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 4, 'Филадельфия с угрем');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 5, 'Филадельфия с красной икрой');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 6, 'Филадельфия с манго-соусом');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 7, 'Ролл с тунцом');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 8, 'Сливочная креветка');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 9, 'Калифорния с креветкой');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 10, 'Калифорния');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 11, 'Опаленный тунец');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 12, 'Рубленный спайси-лосось');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 13, 'Рубленный спайси-тунец');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 14, 'Маки Лосось');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 15, 'Маки Угорь');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 16, 'Маки Креветка');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 17, 'Маки Тунец');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 18, 'Маки огурец');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 19, 'Запеченная креветка');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 20, 'Запеченный с лососем');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 21, 'Запеченный с угрем');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 22, 'Лава с креветками');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 23, 'Лава с лососем');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 24, 'Лава с беконом');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 25, 'Ролл Лава с угрем');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 26, 'Ролл Лава с курицей');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 27, 'Жульен');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 28, 'Лагуна');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 29, 'Лосось моцарелла');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (2, 30, 'Филадельфия запеченная');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 31, 'Нигири Лосось');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 32, 'Нигири тунец');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 33, 'Нигири креветка');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 34, 'Нигири Угорь');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (3, 37, 'Мидии Лава');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (3, 38, 'Мидии Лагуна');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (3, 39, 'Картошка фри');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (3, 40, 'Стрипсы');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (3, 41, 'Спринг-роллы');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 35, 'Гункан спайси угорь');
INSERT INTO public.menu_item (flow_id, id, name) VALUES (1, 36, 'Гункан с креветкой');
