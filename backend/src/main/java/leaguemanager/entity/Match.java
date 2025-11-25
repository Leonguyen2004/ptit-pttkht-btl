package leaguemanager.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Match {

    private Integer id;
    private LocalDate date;
    private LocalTime timeStart;
    private String description;
    // COMPLETED or SCHEDULED
    private String status;

    // Quan hệ N-1
    private Stadium stadium; // Hoặc chỉ lưu tblStadiumid (Integer)
    private Round round;     // Hoặc chỉ lưu tblRoundid (Integer)

    // Quan hệ 1-N
    private List<LeagueTeamMatch> leagueTeamMatches;

    // --- Constructors ---
    public Match(Integer id, LocalDate date, LocalTime timeStart, String description, String status,
                 Stadium stadium, Round round, List<LeagueTeamMatch> leagueTeamMatches) {
        this.id = id;
        this.date = date;
        this.timeStart = timeStart;
        this.description = description;
        this.status = status;
        this.stadium = stadium;
        this.round = round;
        this.leagueTeamMatches = leagueTeamMatches;
    }

    public Match() {
    }

    //getetr setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = timeStart;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Stadium getStadium() {
        return stadium;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public List<LeagueTeamMatch> getLeagueTeamMatches() {
        return leagueTeamMatches;
    }

    public void setLeagueTeamMatches(List<LeagueTeamMatch> leagueTeamMatches) {
        this.leagueTeamMatches = leagueTeamMatches;
    }
}
