INSERT INTO dish(name, dish_type, price) VALUES
('Salade fraîche', 'START', 3500.00),
('Poulet grillé', 'MAIN', 12000.00),
('Riz aux légumes', 'MAIN', NULL),
('Gâteau au chocolat', 'DESSERT', 8000.00),
('Salade de fruits', 'DESSERT', NULL);

INSERT INTO ingredient (name, price, category) VALUES
('Laitue', 800.00, 'VEGETABLE'),
('Tomate' ,600.00, 'VEGETABLE'),
('Poulet', 4500.00, 'ANIMAL'),
('Chocolat', 3000.00, 'OTHER'),
('Beurre', 2500.00, 'DAIRY');

INSERT INTO dishIngredient(id_dish, id_ingredient, quantity_required, unit) VALUES
(1, 1, 0.20, 'KG'),
(1, 2, 0.15, 'KG'),
(2, 3, 1.00, 'KG'),
(4, 4, 0.30, 'KG'),
(4, 5, 0.20, 'KG');