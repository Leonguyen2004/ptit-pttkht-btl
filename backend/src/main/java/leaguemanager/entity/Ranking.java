package leaguemanager.entity;

public class Ranking {
    private int rank;
    private int points;
    // Quan hệ Has-A: Ranking chứa thông tin đội
    private LeagueTeam leagueTeam;

    public Ranking() {
    }

    public Ranking(LeagueTeam leagueTeam) {
        this.leagueTeam = leagueTeam;
        this.points = calculatePoints();
    }

    /**
     * Hàm tính điểm logic bóng đá: Thắng 3, Hòa 1, Thua 0
     * Có xử lý null safety (tránh lỗi nếu database lưu null)
     */
    private int calculatePoints() {
        if (leagueTeam == null) return 0;
        int w = (leagueTeam.getWins() != null) ? leagueTeam.getWins() : 0;
        int d = (leagueTeam.getDraws() != null) ? leagueTeam.getDraws() : 0;
        return (w * 3) + (d * 1);
    }

    // --- Getters & Setters ---

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LeagueTeam getLeagueTeam() {
        return leagueTeam;
    }

    public void setLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeam = leagueTeam;
        // Recalculate points whenever team is set
        this.points = calculatePoints();
    }

    /**
     * Helper để lấy hiệu số bàn thắng bại
     */
    public int getGoalDifference() {
        if (leagueTeam == null) return 0;
        int gf = (leagueTeam.getGoalsFor() != null) ? leagueTeam.getGoalsFor() : 0;
        int ga = (leagueTeam.getGoalsAgainst() != null) ? leagueTeam.getGoalsAgainst() : 0;
        return gf - ga;
    }
}