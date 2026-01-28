import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        String sql = """
                    SELECT id, name, dish_type, price
                    FROM dish
                    WHERE id = ?
                """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Dish not found: " + id);
            }

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("price") == null ? null : rs.getDouble("price"));

            dish.setIngredients(findDishIngredientsByDishId(id));
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dish) {
        String sql = """
                    INSERT INTO dish (name, dish_type, price)
                    VALUES (?, ?::dish_type, ?)
                    ON CONFLICT (id) DO UPDATE
                        SET name = EXCLUDED.name,
                            dish_type = EXCLUDED.dish_type,
                            price = EXCLUDED.price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            Integer dishId = null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishType().name());

                if (dish.getPrice() != null) {
                    ps.setDouble(3, dish.getPrice());
                } else {
                    ps.setNull(3, Types.DOUBLE);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dishId = rs.getInt(1);
                    } else {
                        throw new RuntimeException("Failed to insert/update dish");
                    }
                }
            }

            detachIngredients(conn, dishId, dish.getIngredients());
            attachIngredients(conn, dishId, dish.getIngredients());

            conn.commit();

            return findDishById(dishId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }

        String sql = """
                    INSERT INTO ingredient (name, category, price)
                    VALUES (?, ?::category, ?)
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Ingredient ing : ingredients) {
                    ps.setString(1, ing.getName());
                    ps.setString(2, ing.getCategory().name());

                    if (ing.getPrice() != null) {
                        ps.setDouble(3, ing.getPrice());
                    } else {
                        ps.setNull(3, Types.DOUBLE);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        ing.setId(rs.getInt(1));
                    }
                }
            }

            conn.commit();
            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<DishIngredient> dishIngredients)
            throws SQLException {

        if (dishIngredients == null || dishIngredients.isEmpty()) return;

        for (DishIngredient di : dishIngredients) {
            Ingredient ingredient = di.getIngredient();

            String updateSql = """
                        UPDATE dishingredient
                        SET quantity_required = ?, unit = ?::unit_type
                        WHERE id_dish = ? AND id_ingredient = ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, di.getQuantityRequired());
                ps.setString(2, di.getUnit().name());
                ps.setInt(3, dishId);
                ps.setInt(4, ingredient.getId());

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated == 0) {
                    String insertSql = """
                                INSERT INTO dishingredient (id_dish, id_ingredient, quantity_required, unit)
                                VALUES (?, ?, ?, ?::unit_type)
                            """;
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setInt(1, dishId);
                        psInsert.setInt(2, ingredient.getId());
                        psInsert.setDouble(3, di.getQuantityRequired());
                        psInsert.setString(4, di.getUnit().name());
                        psInsert.executeUpdate();
                    }
                }
            }
        }
    }

    private void detachIngredients(Connection conn, Integer dishId, List<DishIngredient> ingredients) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dishingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        String sql = """
                    SELECT di.id,
                           di.quantity_required,
                           di.unit,
                           i.id AS ingredient_id,
                           i.name,
                           i.price,
                           i.category
                    FROM dishingredient di
                    JOIN ingredient i ON di.id_ingredient = i.id
                    WHERE di.id_dish = ?
                """;

        List<DishIngredient> list = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("ingredient_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                );

                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setIngredient(ingredient);
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(UnitTypeEnum.valueOf(rs.getString("unit")));

                list.add(di);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        String ingredientSql = """
                insert into ingredient (id, name, price, category) values (?, ?, ?, ?::ingredient_category) 
                on conflict (id) do
                update set id = excluded.id, name = excluded.name, 
                price = excluded.price, category = excluded.category::ingredient_category
                returning id, name, price, category;
                """;
        String stockSql = """
                insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?::movement_type, ?::unit_type, ?, ?)
                on conflict (id) do nothing;
                """;
        Connection connection = null;
        try {
            Ingredient savedIngredient = null;
            connection = DBConnection.getDBConnection();
            PreparedStatement ingredientPs = connection.prepareStatement(ingredientSql);

            ingredientPs.setInt(1, toSave.getId());
            ingredientPs.setString(2, toSave.getName());
            ingredientPs.setDouble(3, toSave.getPrice());
            ingredientPs.setString(4, toSave.getCategory().toString());

            ResultSet ingredientRs = ingredientPs.executeQuery();
            if (ingredientRs.next()) {
                savedIngredient = mapToIngredient(ingredientRs);
                savedIngredient.setStockMovementList(toSave.getStockMovementList());
            }
            PreparedStatement stockPs = connection.prepareStatement(stockSql);
            for (StockMovement stockMovement : toSave.getStockMovementList()) {
                stockPs.setInt(1, stockMovement.getId());
                stockPs.setInt(2, toSave.getId());
                stockPs.setDouble(3, stockMovement.getValue().getQuantity());
                stockPs.setString(4, stockMovement.getType().toString());
                stockPs.setString(5, stockMovement.getValue().getUnit().toString());
                stockPs.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                stockPs.addBatch();
            }
            stockPs.executeBatch();
            return savedIngredient;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        String sql = """
                select id, name, price, category from ingredient where id = ?;
                """;
        Connection connection = null;
        Ingredient ingredient = null;
        List<StockMovement> stockMovements = findStockMovementByIngredientId(id);
        try {
            connection = DBConnection.getDBConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ingredient = mapToIngredient(rs);
                ingredient.setStockMovementList(stockMovements);
            }
            return ingredient;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    public Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));

        return ingredient;
    }

    public List<StockMovement> findStockMovementByIngredientId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        String sql = """
                    SELECT id, quantity, type, creation_datetime
                    FROM stock_movement
                    WHERE id_ingredient = ?
                """;

        List<StockMovement> stockMovements = new ArrayList<>();

        try (Connection connection = DBConnection.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement stockMovement = new StockMovement();
                stockMovement.setId(rs.getInt("id"));
                stockMovement.setType(
                        StockMovement.MovementTypeEnum.valueOf(rs.getString("type"))
                );
                stockMovement.setCreationDatetime(
                        rs.getTimestamp("creation_datetime").toInstant()
                );

                StockValue stockValue = new StockValue();
                stockValue.setQuantity(rs.getDouble("quantity"));
                stockMovement.setValue(stockValue);

                stockMovements.add(stockMovement);
            }

            return stockMovements;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Order saveOrder(Order orderToSave) {
        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);
            String orderSql = "INSERT INTO \"Order\" (reference, creation_datetime) VALUES (?, ?) RETURNING id";
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql)) {
                psOrder.setString(1, orderToSave.getReference());
                psOrder.setTimestamp(2, Timestamp.from(Instant.now()));

                ResultSet rs = psOrder.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("Impossible de créer la commande");
                }
                int orderId = rs.getInt(1);
                orderToSave.setId(orderId);
            }
            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                Dish dish = new DataRetriever().findDishById(dishOrder.getDish().getId());
                int quantityOrdered = dishOrder.getQuantity().intValue();
                for (DishIngredient di : dish.getIngredients()) {
                    Ingredient ingredient = new DataRetriever().findIngredientById(di.getIngredient().getId());
                    double currentStock = ingredient.getStockMovementList().stream()
                            .mapToDouble(sm -> sm.getType() == StockMovement.MovementTypeEnum.IN
                                    ? sm.getValue().getQuantity()
                                    : -sm.getValue().getQuantity())
                            .sum();
                    double quantityNeeded = di.getQuantityRequired() * quantityOrdered;

                    if (currentStock < quantityNeeded) {
                        throw new RuntimeException("Stock insuffisant pour l'ingrédient : " + ingredient.getName());
                    }
                }
            }
            String dishOrderSql = "INSERT INTO DishOrder (id_order, id_dish, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement psDishOrder = conn.prepareStatement(dishOrderSql)) {
                for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                    psDishOrder.setInt(1, orderToSave.getId());
                    psDishOrder.setInt(2, dishOrder.getDish().getId());
                    psDishOrder.setDouble(3, dishOrder.getQuantity());
                    psDishOrder.addBatch();
                }
                psDishOrder.executeBatch();
            }
            String stockSql = "INSERT INTO stock_movement (id_ingredient, quantity, type, creation_datetime) VALUES (?, ?, 'OUT', ?)";
            try (PreparedStatement psStock = conn.prepareStatement(stockSql)) {
                for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                    Dish dish = new DataRetriever().findDishById(dishOrder.getDish().getId());
                    int quantityOrdered = dishOrder.getQuantity().intValue();
                    for (DishIngredient di : dish.getIngredients()) {
                        double quantityToRemove = di.getQuantityRequired() * quantityOrdered;
                        psStock.setInt(1, di.getIngredient().getId());
                        psStock.setDouble(2, quantityToRemove);
                        psStock.setTimestamp(3, Timestamp.from(Instant.now()));
                        psStock.addBatch();
                    }
                }
                psStock.executeBatch();
            }
            conn.commit();
            return orderToSave;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order findOrderByReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException("La référence ne peut pas être nulle ou vide");
        }
        String orderSql = "SELECT id, reference, creation_datetime FROM \"Order\" WHERE reference = ?";
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Commande introuvable pour la référence : " + reference);
            }
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
            String dishOrderSql = "SELECT id, id_dish, quantity FROM DishOrder WHERE id_order = ?";
            try (PreparedStatement psDish = conn.prepareStatement(dishOrderSql)) {
                psDish.setInt(1, order.getId());
                ResultSet rsDish = psDish.executeQuery();
                List<DishOrder> dishOrders = new ArrayList<>();
                DataRetriever dataRetriever = new DataRetriever();
                while (rsDish.next()) {
                    DishOrder dishOrder = new DishOrder();
                    dishOrder.setId(rsDish.getInt("id"));
                    dishOrder.setQuantity(rsDish.getInt("quantity"));
                    Dish dish = dataRetriever.findDishById(rsDish.getInt("id_dish"));
                    dishOrder.setDish(dish);
                    dishOrders.add(dishOrder);
                }
                order.setDishOrders(dishOrders);
            }
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public class UnitConverter {
        private static final Map<String, Map<String, Double>> conversionMap = new HashMap<>();

        static {
            // Tomate
            Map<String, Double> tomateMap = new HashMap<>();
            tomateMap.put("KG", 1.0);
            tomateMap.put("PCS", 0.1);
            conversionMap.put("Tomate", tomateMap);

            // Laitue
            Map<String, Double> laitueMap = new HashMap<>();
            laitueMap.put("KG", 1.0);
            laitueMap.put("PCS", 0.5);
            conversionMap.put("Laitue", laitueMap);

            // Chocolat
            Map<String, Double> chocolatMap = new HashMap<>();
            chocolatMap.put("KG", 1.0);
            chocolatMap.put("PCS", 0.1);
            chocolatMap.put("L", 0.4);
            conversionMap.put("Chocolat", chocolatMap);

            // Poulet
            Map<String, Double> pouletMap = new HashMap<>();
            pouletMap.put("KG", 1.0);
            pouletMap.put("PCS", 0.125);
            conversionMap.put("Poulet", pouletMap);

            // Beurre
            Map<String, Double> beurreMap = new HashMap<>();
            beurreMap.put("KG", 1.0);
            beurreMap.put("PCS", 0.25);
            beurreMap.put("L", 0.2);
            conversionMap.put("Beurre", beurreMap);
        }

        public static double convertToReference(String ingredientName, double quantity, String fromUnit) {
            Map<String, Double> ingredientMap = conversionMap.get(ingredientName);
            if (ingredientMap == null || !ingredientMap.containsKey(fromUnit)) {
                throw new RuntimeException(
                        "Conversion impossible pour " + ingredientName + " depuis l'unité " + fromUnit
                );
            }
            return quantity * ingredientMap.get(fromUnit);
        }
    }
}