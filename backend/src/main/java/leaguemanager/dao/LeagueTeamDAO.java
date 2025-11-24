package leaguemanager.dao;

import leaguemanager.entity.League;
import leaguemanager.entity.LeagueTeam;
import leaguemanager.entity.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LeagueTeam entity - handles team registration in leagues
 */
public class LeagueTeamDAO extends DAO {

    /**
     * Get all LeagueTeam records (List of teams registered in leagues)
     * @return List of LeagueTeam objects
     */
    public List<LeagueTeam> getAll() {
        List<LeagueTeam> leagueTeams = new ArrayList<>();

        try {
            // Join 3 bảng: league_team -> league -> team để lấy đầy đủ thông tin
            String sql = "SELECT lt.id, " +
                    "lt.league_id, l.name as league_name, " +
                    "lt.team_id, t.full_name, t.short_name, t.head_coach " +
                    "FROM leagueteam lt " +
                    "JOIN league l ON lt.league_id = l.id " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "ORDER BY lt.id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                leagueTeams.add(mapResultSetToLeagueTeam(rs));
            }

            stmt.close();
            System.out.println("[v0] Retrieved " + leagueTeams.size() + " league-team registrations");

        } catch (SQLException e) {
            System.err.println("[v0] Error fetching all league teams");
            e.printStackTrace();
        }

        return leagueTeams;
    }

    /**
     * Find LeagueTeam by Team Name (Full Name or Short Name)
     * @param nameKeyword Keyword to search in team name
     * @return List of matching LeagueTeam objects
     */
    public List<LeagueTeam> findByName(String nameKeyword) {
        List<LeagueTeam> leagueTeams = new ArrayList<>();

        try {
            String sql = "SELECT lt.id, " +
                    "lt.league_id, l.name as league_name, " +
                    "lt.team_id, t.full_name, t.short_name, t.head_coach " +
                    "FROM leagueteam lt " +
                    "JOIN league l ON lt.league_id = l.id " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "WHERE t.full_name LIKE ? OR t.short_name LIKE ? " +
                    "ORDER BY lt.id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            String queryParam = "%" + nameKeyword + "%";
            stmt.setString(1, queryParam);
            stmt.setString(2, queryParam);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                leagueTeams.add(mapResultSetToLeagueTeam(rs));
            }

            stmt.close();
            System.out.println("[v0] Found " + leagueTeams.size() + " league teams matching '" + nameKeyword + "'");

        } catch (SQLException e) {
            System.err.println("[v0] Error finding league teams by name");
            e.printStackTrace();
        }

        return leagueTeams;
    }

    /**
     * Register a team to a league (Add new LeagueTeam)
     */
    public LeagueTeam add(LeagueTeam leagueTeam) throws Exception {
        // Validate inputs
        if (leagueTeam.getLeague() == null || leagueTeam.getLeague().getId() == null) {
            throw new Exception("League ID is required");
        }
        if (leagueTeam.getTeam() == null || leagueTeam.getTeam().getId() == null) {
            throw new Exception("Team ID is required");
        }

        // Check if already registered
        if (isRegistered(leagueTeam.getLeague().getId(), leagueTeam.getTeam().getId())) {
            throw new Exception("This team is already registered in this league");
        }

        try {
            String sql = "INSERT INTO leagueteam (league_id, team_id) VALUES (?, ?)";

            PreparedStatement stmt = dbCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, leagueTeam.getLeague().getId());
            stmt.setInt(2, leagueTeam.getTeam().getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    leagueTeam.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                stmt.close();
                System.out.println("[v0] Registered team " + leagueTeam.getTeam().getId() + " to league " + leagueTeam.getLeague().getId());
                return leagueTeam;
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error adding league team: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- Helper Methods ---

    private boolean isRegistered(int leagueId, int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leagueteam WHERE league_id = ? AND team_id = ?";
        PreparedStatement stmt = dbCon.prepareStatement(sql);
        stmt.setInt(1, leagueId);
        stmt.setInt(2, teamId);
        ResultSet rs = stmt.executeQuery();
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        stmt.close();
        return exists;
    }

    private LeagueTeam mapResultSetToLeagueTeam(ResultSet rs) throws SQLException {
        LeagueTeam lt = new LeagueTeam();
        lt.setId(rs.getInt("id"));

        // Map League (Partial info)
        League l = new League();
        l.setId(rs.getInt("league_id"));
        l.setName(rs.getString("league_name"));
        lt.setLeague(l);

        // Map Team (Partial info)
        Team t = new Team();
        t.setId(rs.getInt("team_id"));
        t.setFullName(rs.getString("full_name"));
        t.setShortName(rs.getString("short_name"));
        t.setHeadCoach(rs.getString("head_coach"));
        lt.setTeam(t);

        // List matches init empty
        lt.setLeagueTeamMatches(new ArrayList<>());

        return lt;
    }
}