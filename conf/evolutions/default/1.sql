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
    PRIMARY KEY(id)
);

create unique index users_id_unique_index
    on public.users (id);

create unique index users_email_unique_index
    on public.users (email);

create table public.products
(
    id              serial          not null,
    product_name    varchar(64)     not null,
    price           varchar(64)     not null,
    exp_date        decimal         not null,
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

-- !Downs

DROP TABLE IF EXISTS public.order_details;
DROP TABLE IF EXISTS public.orders;
DROP TABLE IF EXISTS public.products;
DROP TABLE IF EXISTS public.users;
