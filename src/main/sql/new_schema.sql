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