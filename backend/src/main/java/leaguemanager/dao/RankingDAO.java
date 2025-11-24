package leaguemanager.dao;

import leaguemanager.entity.League;
import leaguemanager.entity.LeagueTeam;
import leaguemanager.entity.Ranking;
import leaguemanager.entity.Team;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Ranking - Aggregates data from LeagueTeam to build Rankings
 */
public class RankingDAO extends DAO {

    /**
     * Get Ranking list for a specific League.
     * Sorted by: Points (DESC) -> Goal Difference (DESC) -> Goals For (DESC)
     * @param league The league to get rankings for
     * @return List of Ranking objects
     */
    public List<Ranking> getListRankingByLeague(League league) {
        List<Ranking> rankings = new ArrayList<>();

        if (league == null || league.getId() == null) {
            return rankings;
        }

        try {
            // SQL Query thực hiện sắp xếp ngay từ Database để tối ưu hiệu năng.
            // Công thức: (wins * 3 + draws)
            // Sắp xếp ưu tiên: Điểm cao nhất -> Hiệu số tốt nhất -> Bàn thắng nhiều nhất
            String sql = "SELECT lt.id, lt.league_id, lt.wins, lt.draws, lt.losses, lt.goals_for, lt.goals_against, " +
                    "t.id as team_id, t.full_name, t.short_name, " +
                    "(COALESCE(lt.wins, 0) * 3 + COALESCE(lt.draws, 0)) as calculated_points, " +
                    "(COALESCE(lt.goals_for, 0) - COALESCE(lt.goals_against, 0)) as goal_diff " +
                    "FROM leagueteam lt " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "WHERE lt.league_id = ? " +
                    "ORDER BY calculated_points DESC, goal_diff DESC, lt.goals_for DESC";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, league.getId());

            ResultSet rs = stmt.executeQuery();

            int currentRank = 1;
            while (rs.next()) {
                // 1. Construct Team object
                Team team = new Team();
                team.setId(rs.getInt("team_id"));
                team.setFullName(rs.getString("full_name"));
                team.setShortName(rs.getString("short_name"));

                // 2. Construct LeagueTeam object
                LeagueTeam lt = new LeagueTeam();
                lt.setId(rs.getInt("id"));
                lt.setWins(rs.getInt("wins"));
                lt.setDraws(rs.getInt("draws"));
                lt.setLosses(rs.getInt("losses"));
                lt.setGoalsFor(rs.getInt("goals_for"));
                lt.setGoalsAgainst(rs.getInt("goals_against"));
                lt.setTeam(team);
                lt.setLeague(league); // Giữ tham chiếu league ban đầu

                // 3. Create Ranking object
                Ranking ranking = new Ranking(lt);
                ranking.setRank(currentRank++); // Gán rank và tăng dần
                // Lưu ý: constructor Ranking đã tự tính điểm, nhưng nếu muốn lấy từ SQL cũng được
                // ranking.setPoints(rs.getInt("calculated_points"));

                rankings.add(ranking);
            }

            stmt.close();
            System.out.println("[v0] Generated ranking for league " + league.getId() + " with " + rankings.size() + " teams");

        } catch (SQLException e) {
            System.err.println("[v0] Error generating ranking list");
            e.printStackTrace();
        }

        return rankings;
    }
}