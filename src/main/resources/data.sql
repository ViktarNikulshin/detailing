INSERT INTO car_brands (id, name) VALUES
                                      (1, 'Audi'),
                                      (2, 'BMW'),
                                      (3, 'Mercedes-Benz');

INSERT INTO car_models (name, brand_id) VALUES
                                            ('A4', 1),
                                            ('A6', 1),
                                            ('X5', 2),
                                            ('3 Series', 2),
                                            ('E-Class', 3),
                                            ('C-Class', 3);
create table order_work_types
(
    order_id      bigint,
    dictionary_id bigint,
    constraint order_work_types_pk
        primary key (order_id, dictionary_id)
);
