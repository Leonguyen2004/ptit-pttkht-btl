package leaguemanager.dao;

import leaguemanager.entity.League;
import leaguemanager.entity.Round;
import leaguemanager.entity.LeagueTeam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for League entity - handles league data operations
 */
public class LeagueDAO extends DAO {

    /**
     * Find leagues by name (Partial match)
     * @param name The name keyword to search for
     * @return List of matching League objects
     */
    public List<League> findLeagueByName(String name) {
        List<League> leagues = new ArrayList<>();

        try {
            // SQL query tìm kiếm theo tên
            String sql = "SELECT id, name, start_date, end_date, description FROM league WHERE name LIKE ?";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setString(1, "%" + name + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                League league = new League();
                league.setId(rs.getInt("id"));
                league.setName(rs.getString("name"));
                league.setDescription(rs.getString("description"));

                // Xử lý LocalDate (Convert java.sql.Date -> java.time.LocalDate)
                Date sqlStartDate = rs.getDate("start_date");
                if (sqlStartDate != null) {
                    league.setStartDate(sqlStartDate.toLocalDate());
                }

                Date sqlEndDate = rs.getDate("end_date");
                if (sqlEndDate != null) {
                    league.setEndDate(sqlEndDate.toLocalDate());
                }

                // Khởi tạo list rỗng để tránh NullPointerException khi convert JSON
                league.setRounds(new ArrayList<Round>());
                league.setLeagueTeams(new ArrayList<LeagueTeam>());

                leagues.add(league);
            }

            stmt.close();
            System.out.println("[v0] Found " + leagues.size() + " leagues matching '" + name + "'");

        } catch (SQLException e) {
            System.err.println("[v0] Error finding leagues by name");
            e.printStackTrace();
        }

        return leagues;
    }

    /**
     * Add a new league to the database
     * @param league The League object to add
     * @return The added League object with generated ID
     */
    public League add(League league) {
        try {
            String sql = "INSERT INTO league (name, start_date, end_date, description) VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = dbCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, league.getName());

            // Convert java.time.LocalDate -> java.sql.Date
            if (league.getStartDate() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(league.getStartDate()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }

            if (league.getEndDate() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(league.getEndDate()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setString(4, league.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    league.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                stmt.close();
                System.out.println("[v0] Added new league with ID: " + league.getId());
                return league;
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error adding new league: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}