package leaguemanager.dao;

import leaguemanager.entity.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Match entity - handles match data and scheduling logic
 */
public class MatchDAO extends DAO {

    /**
     * Tìm tất cả các trận đấu thuộc một giải đấu
     * @param league League object (cần có ID)
     * @return Match[]
     */
    public List<Match> findMatchByLeague(League league) {
        List<Match> matches = new ArrayList<>();
        if (league == null || league.getId() == null) return matches;

        try {
            // SỬA: "match"
            String sql = "SELECT m.id, m.match_date, m.time_start, m.description, " +
                    "m.stadium_id, s.name as stadium_name, " +
                    "m.round_id, r.name as round_name " +
                    "FROM \"match\" m " +
                    "JOIN round r ON m.round_id = r.id " +
                    "LEFT JOIN stadium s ON m.stadium_id = s.id " +
                    "WHERE r.league_id = ? " +
                    "ORDER BY m.match_date, m.time_start";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, league.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(mapResultSetToMatch(rs));
            }
            stmt.close();

            // Populate thông tin các đội thi đấu
            for (Match m : matches) {
                m.setLeagueTeamMatches(getLeagueTeamMatchesByMatchId(m.getId()));
            }

            System.out.println("[v0] Found " + matches.size() + " matches for league ID: " + league.getId());

        } catch (SQLException e) {
            System.err.println("[v0] Error finding matches by league");
            e.printStackTrace();
        }
        return matches;
    }

    /**
     * Tìm tất cả các trận đấu mà một đội (LeagueTeam) tham gia
     * @param leagueTeam LeagueTeam object (cần có ID)
     * @return Match[]
     */
    public List<Match> findMatchByLeagueTeam(LeagueTeam leagueTeam) {
        List<Match> matches = new ArrayList<>();
        if (leagueTeam == null || leagueTeam.getId() == null) return matches;

        try {
            // SỬA: "match"
            String sql = "SELECT m.id, m.match_date, m.time_start, m.description, " +
                    "m.stadium_id, s.name as stadium_name, " +
                    "m.round_id, r.name as round_name " +
                    "FROM \"match\" m " +
                    "JOIN leagueteammatch ltm ON m.id = ltm.match_id " +
                    "JOIN round r ON m.round_id = r.id " +
                    "LEFT JOIN stadium s ON m.stadium_id = s.id " +
                    "WHERE ltm.league_team_id = ? " +
                    "ORDER BY m.match_date, m.time_start";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, leagueTeam.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(mapResultSetToMatch(rs));
            }
            stmt.close();

            // Populate teams info
            for (Match m : matches) {
                m.setLeagueTeamMatches(getLeagueTeamMatchesByMatchId(m.getId()));
            }

            System.out.println("[v0] Found " + matches.size() + " matches for leagueTeam ID: " + leagueTeam.getId());

        } catch (SQLException e) {
            System.err.println("[v0] Error finding matches by league team");
            e.printStackTrace();
        }
        return matches;
    }

    /**
     * Thêm mới một trận đấu (Match)
     * Kèm logic validate conflict (Sân bận, Đội bận)
     */
    public Match add(Match match) throws Exception {
        // 1. Validate input cơ bản
        if (match.getDate() == null || match.getTimeStart() == null || match.getStadium() == null) {
            throw new Exception("Date, Time and Stadium are required");
        }

        // 2. Lấy danh sách ID của các đội tham gia
        List<Integer> participatingTeamIds = new ArrayList<>();
        if (match.getLeagueTeamMatches() != null) {
            for (LeagueTeamMatch ltm : match.getLeagueTeamMatches()) {
                if (ltm.getLeagueTeam() != null && ltm.getLeagueTeam().getId() != null) {
                    participatingTeamIds.add(ltm.getLeagueTeam().getId());
                }
            }
        }

        if (participatingTeamIds.size() < 2) {
            throw new Exception("A match requires at least 2 teams");
        }

        // 3. Kiểm tra Conflict Sân vận động
        if (isStadiumBooked(match.getStadium().getId(), match.getDate(), match.getTimeStart())) {
            throw new Exception("Conflict: The stadium is booked for another match at this time");
        }

        // 4. Kiểm tra Conflict Đội bóng
        for (Integer ltId : participatingTeamIds) {
            if (isTeamBusy(ltId, match.getDate(), match.getTimeStart())) {
                throw new Exception("Conflict: One of the teams is playing another match at this time");
            }
        }

        // 5. Insert Match vào DB
        try {
            // SỬA: "match" và cột match_date
            String sqlMatch = "INSERT INTO \"match\" (match_date, time_start, description, stadium_id, round_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = dbCon.prepareStatement(sqlMatch, Statement.RETURN_GENERATED_KEYS);

            stmt.setDate(1, java.sql.Date.valueOf(match.getDate()));
            stmt.setTime(2, java.sql.Time.valueOf(match.getTimeStart()));
            stmt.setString(3, match.getDescription());
            stmt.setInt(4, match.getStadium().getId());

            if (match.getRound() != null) {
                stmt.setInt(5, match.getRound().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating match failed, no rows affected.");
            }

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                match.setId(generatedKeys.getInt(1));
            }
            generatedKeys.close();
            stmt.close();

            // 6. Insert LeagueTeamMatch
            String sqlRelation = "INSERT INTO leagueteammatch (match_id, league_team_id) VALUES (?, ?)";
            PreparedStatement stmtRel = dbCon.prepareStatement(sqlRelation);

            for (Integer ltId : participatingTeamIds) {
                stmtRel.setInt(1, match.getId());
                stmtRel.setInt(2, ltId);
                stmtRel.addBatch();
            }
            stmtRel.executeBatch();
            stmtRel.close();

            System.out.println("[v0] Added new match ID: " + match.getId() + " with " + participatingTeamIds.size() + " teams.");
            return match;

        } catch (SQLException e) {
            System.err.println("[v0] Error adding match: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // --- Helper Methods ---

    private boolean isStadiumBooked(Integer stadiumId, LocalDate date, LocalTime time) throws SQLException {
        // SỬA: "match", dùng EXTRACT(EPOCH) thay vì TIMESTAMPDIFF
        String sql = "SELECT COUNT(*) FROM \"match\" " +
                "WHERE stadium_id = ? AND match_date = ? " +
                "AND ABS(EXTRACT(EPOCH FROM time_start) - EXTRACT(EPOCH FROM ?::time)) < 7200";

        PreparedStatement stmt = dbCon.prepareStatement(sql);
        stmt.setInt(1, stadiumId);
        stmt.setDate(2, java.sql.Date.valueOf(date));
        stmt.setTime(3, java.sql.Time.valueOf(time));

        ResultSet rs = stmt.executeQuery();
        boolean isBooked = false;
        if (rs.next()) {
            isBooked = rs.getInt(1) > 0;
        }
        rs.close();
        stmt.close();
        return isBooked;
    }

    private boolean isTeamBusy(Integer leagueTeamId, LocalDate date, LocalTime time) throws SQLException {
        // SỬA: "match", dùng EXTRACT(EPOCH) thay vì TIMESTAMPDIFF
        String sql = "SELECT COUNT(*) FROM leagueteammatch ltm " +
                "JOIN \"match\" m ON ltm.match_id = m.id " +
                "WHERE ltm.league_team_id = ? AND m.match_date = ? " +
                "AND ABS(EXTRACT(EPOCH FROM m.time_start) - EXTRACT(EPOCH FROM ?::time)) < 7200";

        PreparedStatement stmt = dbCon.prepareStatement(sql);
        stmt.setInt(1, leagueTeamId);
        stmt.setDate(2, java.sql.Date.valueOf(date));
        stmt.setTime(3, java.sql.Time.valueOf(time));

        ResultSet rs = stmt.executeQuery();
        boolean isBusy = false;
        if (rs.next()) {
            isBusy = rs.getInt(1) > 0;
        }
        rs.close();
        stmt.close();
        return isBusy;
    }

    private Match mapResultSetToMatch(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getInt("id"));
        m.setDescription(rs.getString("description"));

        Date sqlDate = rs.getDate("match_date");
        if (sqlDate != null) m.setDate(sqlDate.toLocalDate());

        Time sqlTime = rs.getTime("time_start");
        if (sqlTime != null) m.setTimeStart(sqlTime.toLocalTime());

        // Set Stadium info (partial)
        Stadium s = new Stadium();
        s.setId(rs.getInt("stadium_id"));
        s.setName(rs.getString("stadium_name"));
        m.setStadium(s);

        // Set Round info (partial)
        Round r = new Round();
        r.setId(rs.getInt("round_id"));
        r.setName(rs.getString("round_name"));
        m.setRound(r);

        return m;
    }

    // Lấy danh sách các đội tham gia 1 trận đấu cụ thể
    private List<LeagueTeamMatch> getLeagueTeamMatchesByMatchId(Integer matchId) {
        List<LeagueTeamMatch> list = new ArrayList<>();
        try {
            String sql = "SELECT ltm.id, ltm.league_team_id, t.full_name, t.short_name " +
                    "FROM leagueteammatch ltm " +
                    "JOIN leagueteam lt ON ltm.league_team_id = lt.id " +
                    "JOIN team t ON lt.team_id = t.id " +
                    "WHERE ltm.match_id = ?";

            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LeagueTeamMatch ltm = new LeagueTeamMatch();
                ltm.setId(rs.getInt("id"));

                LeagueTeam lt = new LeagueTeam();
                lt.setId(rs.getInt("league_team_id"));

                Team t = new Team();
                t.setFullName(rs.getString("full_name"));
                t.setShortName(rs.getString("short_name"));
                lt.setTeam(t);

                ltm.setLeagueTeam(lt);
                list.add(ltm);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}