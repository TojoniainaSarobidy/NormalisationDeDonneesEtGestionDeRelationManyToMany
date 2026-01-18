import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}