

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.tegel.dao.DatabaseManager;

public class InsertUserTest {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();

            String sql = "INSERT INTO \"mod4db\".\"users\" " +
                        "(email, passwordhash, phonenumber, dateofbirth, dietres, role, full_name, nick_name) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "etnmail@test.com");
            stmt.setString(2, "secureHashedPassword");
            stmt.setString(3, "0123456789");
            stmt.setDate(4, java.sql.Date.valueOf("1995-12-17"));
            stmt.setString(5, "No dietary restrictions");
            stmt.setString(6, "member");
            stmt.setString(7, "Ethan Noronha");
            stmt.setString(8, "actualnick");

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println(" Test user inserted successfully!");
            }

            stmt.close();
        } catch (SQLException e) {
            System.out.println("❌ Insert failed: " + e.getMessage());
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }
}