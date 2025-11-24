package leaguemanager.dao;

import leaguemanager.entity.LeagueTeam;
import leaguemanager.entity.LeagueTeamMatch;
import leaguemanager.entity.Match;
import leaguemanager.entity.Team;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LeagueTeamMatch entity - handles match participation details (stats, goals, cards, etc.)
 */
public class LeagueTeamMatchDAO extends DAO {

    /**
     * Get all LeagueTeamMatch entries for a specific match.
     * This is used to show match details (which teams played, score, etc.)
     * * @param match The Match object (must have ID)
     * @return List of LeagueTeamMatch objects
     */
    public List<LeagueTeamMatch> getLeagueTeamMatchByMatch(Match match) {
        List<LeagueTeamMatch> list = new ArrayList<>();

        if (match == null || match.getId() == null) {
            return list;
        }

        try {
            // Query JOIN: LeagueTeamMatch -> LeagueTeam -> Team
            // Mục đích: Lấy thông tin chỉ số trận đấu kèm theo tên đội bóng
            String sql = "SELECT ltm.id, ltm.role, ltm.goal, ltm.result, ltm.match_id, " +
                    "ltm.league_team_id, t.id as team_id, t.full_name, t.short_name " +
                    "FROM leagueteammatch ltm " +
                    "JOIN leagueteam lt ON ltm.league_team_id = lt.id " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "WHERE ltm.match_id = ? " +
                    "ORDER BY ltm.id";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, match.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LeagueTeamMatch ltm = new LeagueTeamMatch();
                ltm.setId(rs.getInt("id"));
                ltm.setRole(rs.getString("role"));      // Ví dụ: "Home", "Away"
                ltm.setGoal(rs.getObject("goal", Integer.class)); // Cho phép null
                ltm.setResult(rs.getString("result"));  // Ví dụ: "Win", "Lose", "Draw"

                // Map Match info (chỉ giữ ID để tránh lặp vô hạn)
                Match m = new Match();
                m.setId(rs.getInt("match_id"));
                ltm.setMatch(m);

                // Map LeagueTeam & Team info
                LeagueTeam lt = new LeagueTeam();
                lt.setId(rs.getInt("league_team_id"));

                Team t = new Team();
                t.setId(rs.getInt("team_id"));
                t.setFullName(rs.getString("full_name"));
                t.setShortName(rs.getString("short_name"));

                lt.setTeam(t);
                ltm.setLeagueTeam(lt);

                list.add(ltm);
            }

            stmt.close();
            System.out.println("[v0] Found " + list.size() + " participation records for match ID: " + match.getId());

        } catch (SQLException e) {
            System.err.println("[v0] Error finding league team matches by match ID");
            e.printStackTrace();
        }

        return list;
    }
}