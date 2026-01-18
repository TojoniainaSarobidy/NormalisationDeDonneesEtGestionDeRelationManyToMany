import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getDBConnection () {
        String jdbc_url = "jdbc:postgresql://localhost:5432/mini_dish_db";
        String username = "mini_dish_db_manager";
        String password = "123456";
        try {
            return DriverManager.getConnection(jdbc_url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}