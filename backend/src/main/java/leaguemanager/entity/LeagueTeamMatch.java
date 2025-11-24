package leaguemanager.entity;

public class LeagueTeamMatch {

    private Integer id;
    private String role;
    private Integer goal;
    private String result;

    // Quan hệ N-1
    private LeagueTeam leagueTeam; // Hoặc chỉ lưu tblLeagueTeamid (Integer)
    private Match match;           // Hoặc chỉ lưu tblMatchid (Integer)

    // Constructors ---
    public LeagueTeamMatch(Integer id, String role, Integer goal, String result, LeagueTeam leagueTeam, Match match) {
        this.id = id;
        this.role = role;
        this.goal = goal;
        this.result = result;
        this.leagueTeam = leagueTeam;
        this.match = match;
    }
    public LeagueTeamMatch() {
    }

    //getter setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LeagueTeam getLeagueTeam() {
        return leagueTeam;
    }

    public void setLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeam = leagueTeam;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}
