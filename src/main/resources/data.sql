----------------User-----------------
INSERT INTO users(userId, email, password) VALUES('00001', 'jialuo.chen@mail.utoronto.ca', '1111');
INSERT INTO users(userId, email, password) VALUES('00002', 'lea.kwon@mail.utoronto.ca', '1234');

----------------Skintypes-----------------
INSERT INTO skintypes(skintypeId, description) VALUES (1, 'oily');
INSERT INTO skintypes(skintypeId, description) VALUES (2, 'dry');
INSERT INTO skintypes(skintypeId, description) VALUES (3, 'combination');

----------------Concerns-----------------
INSERT INTO concerns(concernId, description) VALUES (1, 'wrinkles');
INSERT INTO concerns(concernId, description) VALUES (2, 'acne');
INSERT INTO concerns(concernId, description) VALUES (3, 'pores');
INSERT INTO concerns(concernId, description) VALUES (4, 'hyperpigmentation');
INSERT INTO concerns(concernId, description) VALUES (5, 'irritation');
INSERT INTO concerns(concernId, description) VALUES (6, 'dull skin');

----------------Products-----------------




----------------Ingredients-----------------
INSERT INTO ingredients (ingredientId, ingredientName) VALUES (1, 'Water');
INSERT INTO ingredients (ingredientId, ingredientName) VALUES (2, 'Aloe Barbadensis Leaf Juice');


----------------testResults-----------------
--INSERT INTO testResults(testResultId, user, recommendedProducts, concerns, avoidIngredients, budget, skinType)
--VALUES (1);

