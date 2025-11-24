package leaguemanager.dto;

public class RankingDTO {
    private String teamName;
    private String teamLogo;
    private Integer leagueTeamId;

    // Các thuộc tính dẫn xuất (Derived attributes)
    private int played;       // Số trận đã đấu
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;     // Bàn thắng
    private int goalsAgainst; // Bàn thua
    private int goalDifference; // Hiệu số
    private int points;       // Điểm
    private int rank;         // Thứ hạng

    public RankingDTO() {
    }

    public RankingDTO(String teamName, String teamLogo, Integer leagueTeamId, int played, int won, int drawn, int lost, int goalsFor, int goalsAgainst, int points) {
        this.teamName = teamName;
        this.teamLogo = teamLogo;
        this.leagueTeamId = leagueTeamId;
        this.played = played;
        this.won = won;
        this.drawn = drawn;
        this.lost = lost;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalsFor - goalsAgainst;
        this.points = points;
    }

    // Getters and Setters
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamLogo() { return teamLogo; }
    public void setTeamLogo(String teamLogo) { this.teamLogo = teamLogo; }

    public Integer getLeagueTeamId() { return leagueTeamId; }
    public void setLeagueTeamId(Integer leagueTeamId) { this.leagueTeamId = leagueTeamId; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getDrawn() { return drawn; }
    public void setDrawn(int drawn) { this.drawn = drawn; }

    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }

    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }

    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }

    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
