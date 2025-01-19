create table employee
(
    salary double precision,
    id     bigint default nextval('employee_id_generator'::regclass) not null
        primary key,
    name   varchar(255)                                              not null
        constraint uq_employee_name
            unique
);

alter table employee
    owner to root;

INSERT INTO public.employee (salary, id, name) VALUES (null, 1, 'Кичигин Д.');
