package leaguemanager.entity;

import java.util.List;

public class LeagueTeam {

    private Integer id;
//    private Integer wins;
//    private Integer draws;
//    private Integer losses;
//    private Integer goalsFor;      // Bàn thắng
//    private Integer goalsAgainst;  // Bàn thua

    // Quan hệ N-1
    private League league;
    private Team team;

    // Quan hệ 1-N
    private List<LeagueTeamMatch> leagueTeamMatches;

    // --- Constructors ---

    // Constructor mặc định
    public LeagueTeam() {
    }

    // Constructor đầy đủ tham số
    public LeagueTeam(Integer id, Integer wins, Integer draws, Integer losses,
                      Integer goalsFor, Integer goalsAgainst,
                      League league, Team team, List<LeagueTeamMatch> leagueTeamMatches) {
        this.id = id;
        this.league = league;
        this.team = team;
        this.leagueTeamMatches = leagueTeamMatches;
    }

    // --- Getters and Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<LeagueTeamMatch> getLeagueTeamMatches() {
        return leagueTeamMatches;
    }

    public void setLeagueTeamMatches(List<LeagueTeamMatch> leagueTeamMatches) {
        this.leagueTeamMatches = leagueTeamMatches;
    }
}