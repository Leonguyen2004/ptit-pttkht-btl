package leaguemanager.dao;

import leaguemanager.dto.RankingDTO;
import leaguemanager.entity.League;

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
     * @return List of RankingDTO objects
     */
    public List<RankingDTO> getListRankingByLeague(League league) {
        List<RankingDTO> rankings = new ArrayList<>();

        if (league == null || league.getId() == null) {
            return rankings;
        }

        try {
            // Revised SQL Strategy:
            // 1. Get all matches for the league.
            // 2. For each match, we have 2 rows in leagueteammatch (Home and Away).
            // 3. We need to self-join or use window functions to get the opponent's score.
            // Calculate stats for each team-match, then aggregate.

            String sqlRevised = "WITH MatchResults AS ( " +
                    "    SELECT " +
                    "        ltm.league_team_id, " +
                    "        ltm.goal AS goals_for, " +
                    "        (SELECT ltm2.goal FROM leagueteammatch ltm2 WHERE ltm2.match_id = ltm.match_id AND ltm2.id != ltm.id) AS goals_against " +
                    "    FROM leagueteammatch ltm " +
                    "    JOIN \"match\" m ON ltm.match_id = m.id " +
                    "    JOIN round r ON m.round_id = r.id " +
                    "    WHERE r.league_id = ? AND m.status = 'COMPLETED' " +
                    ") " +
                    "SELECT " +
                    "    lt.id AS league_team_id, " +
                    "    t.full_name AS team_name, " +
                    "    t.logo AS team_logo, " +
                    "    COUNT(mr.league_team_id) AS played, " +
                    "    SUM(CASE WHEN mr.goals_for > mr.goals_against THEN 1 ELSE 0 END) AS won, " +
                    "    SUM(CASE WHEN mr.goals_for = mr.goals_against THEN 1 ELSE 0 END) AS drawn, " +
                    "    SUM(CASE WHEN mr.goals_for < mr.goals_against THEN 1 ELSE 0 END) AS lost, " +
                    "    COALESCE(SUM(mr.goals_for), 0) AS goals_for, " +
                    "    COALESCE(SUM(mr.goals_against), 0) AS goals_against, " +
                    "    SUM(CASE WHEN mr.goals_for > mr.goals_against THEN 3 " +
                    "             WHEN mr.goals_for = mr.goals_against THEN 1 ELSE 0 END) AS points " +
                    "FROM leagueteam lt " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "LEFT JOIN MatchResults mr ON lt.id = mr.league_team_id " +
                    "WHERE lt.league_id = ? " +
                    "GROUP BY lt.id, t.full_name, t.logo " +
                    "ORDER BY points DESC, (COALESCE(SUM(mr.goals_for), 0) - COALESCE(SUM(mr.goals_against), 0)) DESC, COALESCE(SUM(mr.goals_for), 0) DESC";

            PreparedStatement stmt = dbCon.prepareStatement(sqlRevised);
            stmt.setInt(1, league.getId());
            stmt.setInt(2, league.getId());

            ResultSet rs = stmt.executeQuery();

            int currentRank = 1;
            while (rs.next()) {
                RankingDTO ranking = new RankingDTO();
                ranking.setLeagueTeamId(rs.getInt("league_team_id"));
                ranking.setTeamName(rs.getString("team_name"));
                ranking.setTeamLogo(rs.getString("team_logo"));
                ranking.setPlayed(rs.getInt("played"));
                ranking.setWon(rs.getInt("won"));
                ranking.setDrawn(rs.getInt("drawn"));
                ranking.setLost(rs.getInt("lost"));
                ranking.setGoalsFor(rs.getInt("goals_for"));
                ranking.setGoalsAgainst(rs.getInt("goals_against"));
                ranking.setGoalDifference(ranking.getGoalsFor() - ranking.getGoalsAgainst());
                ranking.setPoints(rs.getInt("points"));
                ranking.setRank(currentRank++);

                rankings.add(ranking);
            }

            stmt.close();
            System.out.println("[v0] Generated dynamic ranking for league " + league.getId() + " with " + rankings.size() + " teams");

        } catch (SQLException e) {
            System.err.println("[v0] Error generating ranking list");
            e.printStackTrace();
        }

        return rankings;
    }
}