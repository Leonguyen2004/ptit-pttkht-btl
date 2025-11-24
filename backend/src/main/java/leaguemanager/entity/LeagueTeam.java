package leaguemanager.entity;

import java.util.List;

public class LeagueTeam {

    private Integer id;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;      // Bàn thắng
    private Integer goalsAgainst;  // Bàn thua

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
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
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

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getDraws() {
        return draws;
    }

    public void setDraws(Integer draws) {
        this.draws = draws;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public Integer getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(Integer goalsFor) {
        this.goalsFor = goalsFor;
    }

    public Integer getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(Integer goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
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