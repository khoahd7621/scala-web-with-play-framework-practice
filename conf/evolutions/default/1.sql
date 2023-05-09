DROP TABLE IF EXISTS public.order_details;
DROP TABLE IF EXISTS public.orders;
DROP TABLE IF EXISTS public.products;
DROP TABLE IF EXISTS public.users;

-- !Ups

create table public.users
(
    id              serial          not null,
    email           varchar(100)    not null,
    first_name      varchar(64)     not null,
    last_name       varchar(64)     not null,
    password        varchar(128)    not null,
    role            varchar(16)     not null,
    birth_date      timestamp       not null,
    phone_number    varchar(12)     not null,
    address         varchar(255)    not null,
    PRIMARY KEY(id)
);

create unique index users_id_unique_index
    on public.users (id);

create unique index users_email_unique_index
    on public.users (email);

create table public.products
(
    id              serial          not null,
    product_name    varchar(255)     not null,
    price           decimal         not null,
    exp_date        timestamp       not null,
    PRIMARY KEY(id)
);

create unique index products_id_unique_index
    on public.products (id);

create table public.orders
(
    id              serial      not null,
    user_id         serial      not null,
    order_date      timestamp   not null,
    total_price     decimal     not null,
    PRIMARY KEY(id),
    CONSTRAINT fk_customer FOREIGN KEY(user_id) REFERENCES public.users(id)
);

create unique index orders_id_unique_index
    on public.orders (id);

create table public.order_details
(
    id              serial      not null,
    order_id        serial      not null,
    product_id      serial      not null,
    quantity        int         not null,
    price           decimal     not null,
    PRIMARY KEY(id),
    CONSTRAINT fk_orders    FOREIGN KEY(order_id)   REFERENCES public.orders(id),
    CONSTRAINT fk_products  FOREIGN KEY(product_id) REFERENCES public.products(id)
);

create unique index order_details_id_unique_index
    on public.order_details (id);

INSERT INTO public.users (email,first_name,last_name,"password","role",birth_date,phone_number,address) VALUES
                                                                                                            ('admin@nashtechglobal.com','Administrator','System','$2a$10$zq/9Vb4YTBKegEXy7VOM2ekJgo1empmuzYsf4ppH8/zU5seqcH8IW','Admin','1970-01-01 00:00:00.000','0123456789','Vietnam'),
                                                                                                            ('operator@nashtechglobal.com','Operator','System','$2a$10$7VuJ1AZh2OumhrqhFDgYK.Qb8y23SbVrsdxyuQceLhBaPy5qt847K','Operator','1980-01-01 00:00:00.000','0123456789','Vietnam'),
                                                                                                            ('user@nashtechglobal.com','User','Customer','$2a$10$hZEnlSZzgdrQIwgg04x/UepeNYjfKQSfgdI6h1Al24v5NVs/gp0/e','User','1980-01-01 00:00:00.000','0123456789','Vietnam');

INSERT INTO public.products (product_name,price,exp_date) VALUES
                                                              ('Samsung Galaxy S22 Ultra 1Tb 8GB',29880000,'2024-12-31 00:00:00.000'),
                                                              ('Keyboard Akko Tokyo Ver2 LED RGB Bluetooth',2990000,'2024-12-31 00:00:00.000'),
                                                              ('Mouse Logitech G403 V2 White',399000,'2024-12-31 00:00:00.000'),
                                                              ('Headphone Sony WH-1000 XM5 HiRes Audio',8450000,'2024-12-31 00:00:00.000'),
                                                              ('Monitor Asus 4k Graphic Design Pro',12320000,'2024-12-31 00:00:00.000');

INSERT INTO public.orders (user_id,order_date,total_price) VALUES
    (3,'2023-05-07 10:46:16.230793',62750000);

INSERT INTO public.order_details (order_id,product_id,quantity,price) VALUES
                                                                          (1,1,2,29880000),
                                                                          (1,2,1,2990000);

-- !Downs

DROP TABLE IF EXISTS public.order_details;
DROP TABLE IF EXISTS public.orders;
DROP TABLE IF EXISTS public.products;
DROP TABLE IF EXISTS public.users;
