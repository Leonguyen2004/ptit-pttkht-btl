package leaguemanager.entity;

import java.util.List;

public class Team {

    private Integer id;
    private String fullName;
    private String shortName;
    private String headCoach;
    private String homeKitColor;
    private String awayKitColor;
    private String achievements;

    // Thêm trường logo để lưu đường dẫn ảnh
    private String logo;

    // Quan hệ N-1
    private Stadium stadium;

    // Quan hệ 1-N
    private List<LeagueTeam> leagueTeams;

    // ---Constructors ---
    // Cập nhật Constructor đầy đủ
    public Team(Integer id, String fullName, String shortName, String headCoach, String homeKitColor, String awayKitColor, String achievements, String logo, Stadium stadium, List<LeagueTeam> leagueTeams) {
        this.id = id;
        this.fullName = fullName;
        this.shortName = shortName;
        this.headCoach = headCoach;
        this.homeKitColor = homeKitColor;
        this.awayKitColor = awayKitColor;
        this.achievements = achievements;
        this.logo = logo;
        this.stadium = stadium;
        this.leagueTeams = leagueTeams;
    }

    public Team() {
    }

    //getter setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getHeadCoach() {
        return headCoach;
    }

    public void setHeadCoach(String headCoach) {
        this.headCoach = headCoach;
    }

    public String getHomeKitColor() {
        return homeKitColor;
    }

    public void setHomeKitColor(String homeKitColor) {
        this.homeKitColor = homeKitColor;
    }

    public String getAwayKitColor() {
        return awayKitColor;
    }

    public void setAwayKitColor(String awayKitColor) {
        this.awayKitColor = awayKitColor;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Stadium getStadium() {
        return stadium;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public List<LeagueTeam> getLeagueTeams() {
        return leagueTeams;
    }

    public void setLeagueTeams(List<LeagueTeam> leagueTeams) {
        this.leagueTeams = leagueTeams;
    }
}