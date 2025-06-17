

import java.sql.Connection;
import java.sql.SQLException;

import com.tegel.dao.DatabaseManager;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn != null) {
                System.out.println(" Connection test successful!");
            }
        } catch (SQLException e) {
            System.out.println(" Connection test failed: " + e.getMessage());
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }
}