CREATE TYPE category AS enum (
    'VEGETABLE',
    'ANIMAL',
    'MARINE',
    'DAIRY',
    'OTHER'
);

CREATE TYPE dish_type AS enum (
    'START',
    'MAIN',
    'DESSERT'
);

CREATE TYPE unit_type AS enum (
    'PCS',
    'KG',
    'L'
);

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    price NUMERIC(10,2),
    category category NOT NULL
);

CREATE TABLE dishIngredient (
    id SERIAL PRIMARY KEY,
    id_dish int,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES dish(id),
    id_ingredient int,
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id),
    quantity_required numeric(10, 2),
    unit unit_type
);

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    dish_type dish_type,
    price NUMERIC(10,2)
);

CREATE TABLE "Order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(100),
    creation_datetime TIMESTAMP
);

CREATE TABLE DishOrder (
    id SERIAL PRIMARY KEY,
    id_order int,
    id_dish int,
    quantity numeric(10,2),
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES "Order"(id),
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES dish(id)
);

CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    table_number INTEGER NOT NULL UNIQUE
);


ALTER TABLE DishOrder
ADD COLUMN table_id INTEGER,
ADD COLUMN installation_datetime TIMESTAMP,
ADD COLUMN departure_datetime TIMESTAMP,
ADD CONSTRAINT fk_order_table
    FOREIGN KEY (table_id) REFERENCES restaurant_table(id);