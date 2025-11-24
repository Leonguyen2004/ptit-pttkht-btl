package leaguemanager.dao;

import leaguemanager.entity.Team;
import leaguemanager.entity.Stadium;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Team entity - handles team data operations
 */
public class TeamDAO extends DAO {

    /**
     * Get all teams from database
     * @return List of all Team objects
     */
    public List<Team> getListTeam() {
        List<Team> teams = new ArrayList<>();

        try {
            String sql = "SELECT t.id, t.full_name, t.short_name, t.head_coach, " +
                    "t.home_kit_color, t.away_kit_color, t.achievements, t.logo, t.stadium_id, " +
                    "s.id as stadium_id, s.name as stadium_name, s.address as stadium_address, s.capacity as stadium_capacity " +
                    "FROM team t " +
                    "LEFT JOIN stadium s ON t.stadium_id = s.id " +
                    "ORDER BY t.id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Team team = new Team();
                team.setId(rs.getInt("id"));
                team.setFullName(rs.getString("full_name"));
                team.setShortName(rs.getString("short_name"));
                team.setHeadCoach(rs.getString("head_coach"));
                team.setHomeKitColor(rs.getString("home_kit_color"));
                team.setAwayKitColor(rs.getString("away_kit_color"));
                team.setAchievements(rs.getString("achievements"));

                // Lấy dữ liệu logo
                team.setLogo(rs.getString("logo"));

                Integer stadiumId = rs.getObject("stadium_id", Integer.class);
                if (stadiumId != null && !rs.wasNull()) {
                    Stadium stadium = new Stadium();
                    stadium.setId(rs.getInt("stadium_id"));
                    stadium.setName(rs.getString("stadium_name"));
                    stadium.setAddress(rs.getString("stadium_address"));
                    stadium.setCapacity(rs.getObject("stadium_capacity", Integer.class));
                    team.setStadium(stadium);
                }

                teams.add(team);
            }

            stmt.close();
            System.out.println("[v0] Retrieved " + teams.size() + " teams from database");

        } catch (SQLException e) {
            System.err.println("[v0] Error fetching teams");
            e.printStackTrace();
        }

        return teams;
    }

    /**
     * Add a new team to the database
     * @param team The Team object to add
     * @return The added Team object with the generated ID, or null if failed
     */
    public Team addTeam(Team team) {
        try {
            // Thêm cột logo vào INSERT
            String sql = "INSERT INTO team (full_name, short_name, head_coach, home_kit_color, away_kit_color, achievements, logo, stadium_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = dbCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, team.getFullName());
            stmt.setString(2, team.getShortName());
            stmt.setString(3, team.getHeadCoach());
            stmt.setString(4, team.getHomeKitColor());
            stmt.setString(5, team.getAwayKitColor());
            stmt.setString(6, team.getAchievements());

            // Set giá trị logo
            stmt.setString(7, team.getLogo());

            // Xử lý foreign key stadium_id
            if (team.getStadium() != null && team.getStadium().getId() != null) {
                stmt.setInt(8, team.getStadium().getId());
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID vừa sinh ra
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    team.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                stmt.close();
                System.out.println("[v0] Added new team with ID: " + team.getId());
                return team;
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error adding new team: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}