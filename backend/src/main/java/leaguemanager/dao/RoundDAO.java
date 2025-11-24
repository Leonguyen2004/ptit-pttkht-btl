package leaguemanager.dao;

import leaguemanager.entity.League;
import leaguemanager.entity.Round;
import leaguemanager.entity.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Round entity - handles round data operations
 */
public class RoundDAO extends DAO {

    /**
     * Find rounds belonging to a specific league
     * @param league The League object (must have ID set)
     * @return List of Round objects
     */
    public List<Round> findRoundByLeague(League league) {
        List<Round> rounds = new ArrayList<>();

        if (league == null || league.getId() == null) {
            return rounds;
        }

        try {
            String sql = "SELECT id, name, start_date, end_date, description, league_id " +
                    "FROM round " +
                    "WHERE league_id = ? " +
                    "ORDER BY id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, league.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rounds.add(mapResultSetToRound(rs, league));
            }

            stmt.close();
            System.out.println("[v0] Found " + rounds.size() + " rounds for league ID: " + league.getId());

        } catch (SQLException e) {
            System.err.println("[v0] Error finding rounds by league");
            e.printStackTrace();
        }

        return rounds;
    }

    /**
     * Find rounds by name within a specific league
     * @param name The name keyword to search for
     * @param league The League object (must have ID set)
     * @return List of matching Round objects
     */
    public List<Round> findRoundByNameAndLeague(String name, League league) {
        List<Round> rounds = new ArrayList<>();

        if (league == null || league.getId() == null) {
            return rounds;
        }

        try {
            String sql = "SELECT id, name, start_date, end_date, description, league_id " +
                    "FROM round " +
                    "WHERE league_id = ? AND name LIKE ? " +
                    "ORDER BY id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, league.getId());
            stmt.setString(2, "%" + name + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rounds.add(mapResultSetToRound(rs, league));
            }

            stmt.close();
            System.out.println("[v0] Found " + rounds.size() + " rounds matching '" + name + "' in league ID: " + league.getId());

        } catch (SQLException e) {
            System.err.println("[v0] Error finding rounds by name and league");
            e.printStackTrace();
        }

        return rounds;
    }

    /**
     * Helper method to map ResultSet to Round object
     */
    private Round mapResultSetToRound(ResultSet rs, League contextLeague) throws SQLException {
        Round round = new Round();
        round.setId(rs.getInt("id"));
        round.setName(rs.getString("name"));
        round.setDescription(rs.getString("description"));

        // Convert java.sql.Date -> java.time.LocalDate
        Date sqlStartDate = rs.getDate("start_date");
        if (sqlStartDate != null) {
            round.setStartDate(sqlStartDate.toLocalDate());
        }

        Date sqlEndDate = rs.getDate("end_date");
        if (sqlEndDate != null) {
            round.setEndDate(sqlEndDate.toLocalDate());
        }

        // Set the league relationship
        // Nếu contextLeague truyền vào chưa đủ thông tin, có thể query thêm,
        // nhưng ở đây ta gán object truyền vào để giữ reference.
        round.setLeague(contextLeague);

        // Init empty list for matches
        round.setMatches(new ArrayList<Match>());

        return round;
    }
}