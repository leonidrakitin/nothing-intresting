create table flow_step
(
    id         integer default nextval('flow_step_id_generator'::regclass) not null
        primary key,
    step_order integer                                                     not null,
    flow_id    bigint                                                      not null
        constraint fk_flow_step_flow_id_revinfo_rev
            references flow,
    station_id bigint                                                      not null
        constraint fk_flow_step_station_id_invoice_act_id
            references station,
    step_type  varchar(255)                                                not null,
    constraint uq_flow_step_flow_id_step_order
        unique (flow_id, step_order)
);

alter table flow_step
    owner to root;

INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (1, 1, 1, 2, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (2, 2, 1, 4, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (10, 0, 1, 5, 'FINAL_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (3, 1, 2, 2, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (4, 2, 2, 3, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (5, 3, 2, 4, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (8, 0, 2, 5, 'FINAL_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (6, 1, 3, 3, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (7, 2, 3, 4, 'PROGRESS_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (9, 0, 3, 5, 'FINAL_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (11, -1, 1, 6, 'FINAL_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (12, -1, 2, 6, 'FINAL_STEP');
INSERT INTO public.flow_step (id, step_order, flow_id, station_id, step_type) VALUES (13, -1, 3, 6, 'FINAL_STEP');
