package leaguemanager.dao;

import leaguemanager.entity.Stadium;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Stadium entity - handles stadium data operations
 */
public class StadiumDAO extends DAO {

    /**
     * Find stadiums by name (Partial match)
     * @param name The name keyword to search for
     * @return List of matching Stadium objects
     */
    public List<Stadium> findStadiumByName(String name) {
        List<Stadium> stadiums = new ArrayList<>();

        try {
            String sql = "SELECT id, name, address, capacity FROM stadium WHERE name LIKE ?";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            // Sử dụng % để tìm kiếm gần đúng (LIKE)
            stmt.setString(1, "%" + name + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Stadium stadium = new Stadium();
                stadium.setId(rs.getInt("id"));
                stadium.setName(rs.getString("name"));
                stadium.setAddress(rs.getString("address"));
                stadium.setCapacity(rs.getObject("capacity", Integer.class));

                // Lưu ý: Lists teams và matches thường sẽ được lazy load hoặc query riêng
                // nên ở đây để null hoặc khởi tạo list rỗng
                stadium.setTeams(new ArrayList<>());
                stadium.setMatches(new ArrayList<>());

                stadiums.add(stadium);
            }

            stmt.close();
            System.out.println("[v0] Found " + stadiums.size() + " stadiums matching '" + name + "'");

        } catch (SQLException e) {
            System.err.println("[v0] Error finding stadiums by name");
            e.printStackTrace();
        }

        return stadiums;
    }

    /**
     * Add a new stadium to the database
     * @param stadium The Stadium object to add
     * @return The added Stadium object with the generated ID, or null if failed
     */
    public Stadium addStadium(Stadium stadium) {
        try {
            String sql = "INSERT INTO stadium (name, address, capacity) VALUES (?, ?, ?)";

            PreparedStatement stmt = dbCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, stadium.getName());
            stmt.setString(2, stadium.getAddress());
            if (stadium.getCapacity() != null) {
                stmt.setInt(3, stadium.getCapacity());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    stadium.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                stmt.close();
                System.out.println("[v0] Added new stadium with ID: " + stadium.getId());
                return stadium;
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error adding new stadium: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}