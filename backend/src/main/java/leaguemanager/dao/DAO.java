package leaguemanager.dao;

import java.sql.Connection;

public abstract class DAO {
    // Lớp DAO cha giữ kết nối
    protected Connection dbCon;

    public DAO() {
        // Khi một DAO được tạo, nó tự động lấy kết nối
        this.dbCon = DBConnection.getInstance().getConnection();
    }
}
