package leaguemanager.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Dùng Singleton để quản lý kết nối CSDL
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private String url = "jdbc:postgresql://localhost:5432/league_manager"; // Thay tên CSDL
    private String user = "postgres"; // Thay user
    private String password = "123456"; // Thay password

    // Constructor là private để không ai tạo mới được
    private DBConnection() {
        try {
            // 1. Nạp driver
            Class.forName("org.postgresql.Driver");
            // 2. Tạo kết nối
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            // Xử lý lỗi nghiêm trọng ở đây (ví dụ: không thể kết nối CSDL)
        }
    }

    // Phương thức public để lấy thể hiện (instance) duy nhất
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Phương thức để các DAO dùng chung kết nối
    public Connection getConnection() {
        return connection;
    }
}
