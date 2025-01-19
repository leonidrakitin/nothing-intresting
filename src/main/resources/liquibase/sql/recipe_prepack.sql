create table recipe_prepack
(
    final_amount        double precision                                                not null,
    init_amount         double precision                                                not null,
    losses_amount       double precision                                                not null,
    losses_percentage   double precision                                                not null,
    id                  bigint default nextval('prepack_recipe_id_generator'::regclass) not null
        primary key,
    prepack_id          bigint                                                          not null
        constraint fk_recipe_prepack_prepack_id_station_id
            references prepack,
    source_id           bigint                                                          not null,
    source_type         varchar(255)                                                    not null,
    priority            bigint default 100                                              not null,
    measurement_unit_id bigint
);

alter table recipe_prepack
    owner to root;

INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (20, 20, 0, 0, 1, 2, 52, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (95, 95, 0, 0, 3, 2, 48, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (57, 57, 0, 0, 4, 2, 24, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (95, 95, 0, 0, 5, 2, 17, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (190, 190, 0, 0, 6, 2, 22, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (535, 535, 0, 0, 7, 2, 58, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (8, 8, 0, 0, 2, 2, 51, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (500, 500, 0, 0, 8, 3, 32, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (250, 250, 0, 0, 59, 9, 2, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 10, 4, 18, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (400, 400, 0, 0, 11, 4, 48, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (100, 100, 0, 0, 12, 4, 47, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (50, 50, 0, 0, 13, 4, 51, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (20, 20, 0, 0, 14, 4, 37, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (15, 15, 0, 0, 15, 5, 18, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (55, 55, 0, 0, 16, 5, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (300, 300, 0, 0, 17, 5, 4, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (120, 120, 0, 0, 18, 5, 26, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (0, 0, 0, 0, 19, 5, 62, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (100, 100, 0, 0, 21, 6, 48, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (160, 160, 0, 0, 20, 6, 47, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (40, 40, 0, 0, 22, 6, 51, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (4, 4, 0, 0, 23, 6, 46, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (20, 20, 0, 0, 24, 6, 43, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (60, 60, 0, 0, 25, 6, 42, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (4, 4, 0, 0, 26, 6, 54, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (3000, 3000, 0, 0, 27, 7, 58, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (900, 900, 0, 0, 28, 7, 17, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (100, 100, 0, 0, 29, 7, 48, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (20, 20, 0, 0, 30, 7, 50, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (60, 60, 0, 0, 31, 7, 51, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 32, 8, 39, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1500, 1500, 0, 0, 33, 8, 58, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 34, 10, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (280, 280, 0, 0, 35, 10, 13, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (60, 60, 0, 0, 37, 10, 20, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (20, 20, 0, 0, 38, 10, 42, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1300, 1300, 0, 0, 9, 3, 12, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (700, 700, 0, 0, 36, 10, 25, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 39, 11, 13, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 40, 11, 25, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (800, 800, 0, 0, 41, 11, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 42, 13, 16, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 43, 13, 25, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (500, 500, 0, 0, 44, 13, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 45, 14, 11, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1100, 1100, 0, 0, 46, 14, 58, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (200, 200, 0, 0, 47, 14, 4, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (450, 450, 0, 0, 48, 24, 9, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (375, 375, 0, 0, 49, 24, 30, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (12, 12, 0, 0, 50, 24, 10, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (12, 12, 0, 0, 51, 24, 8, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (90, 90, 0, 0, 52, 24, 23, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (30, 30, 0, 0, 53, 24, 22, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (80, 80, 0, 0, 54, 24, 7, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (6, 6, 0, 0, 55, 24, 47, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (15, 15, 0, 0, 56, 24, 21, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (15, 15, 0, 0, 57, 24, 44, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (2, 2, 0, 0, 58, 24, 57, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (80, 10, 0, 0, 60, 9, 41, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (50, 50, 0, 0, 61, 9, 14, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (50, 50, 0, 0, 62, 9, 13, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (150, 150, 0, 0, 63, 9, 25, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (150, 150, 0, 0, 64, 9, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (2, 2, 0, 0, 66, 9, 42, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (2, 2, 0, 0, 65, 9, 43, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (500, 500, 0, 0, 67, 12, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (500, 500, 0, 0, 68, 12, 14, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (180, 180, 0, 0, 69, 12, 25, 'PREPACK', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1000, 1000, 0, 0, 70, 1, 15, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (350, 350, 0, 0, 71, 1, 20, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (40, 40, 0, 0, 72, 1, 21, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (5, 5, 0, 0, 73, 1, 36, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (100, 100, 0, 0, 74, 1, 22, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (1, 1, 0, 0, 75, 27, 63, 'INGREDIENT', 1, null);
INSERT INTO public.recipe_prepack (final_amount, init_amount, losses_amount, losses_percentage, id, prepack_id, source_id, source_type, priority, measurement_unit_id) VALUES (30, 30, 0, 0, 76, 27, 24, 'PREPACK', 1, null);
