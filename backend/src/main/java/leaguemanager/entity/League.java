package leaguemanager.entity;

import java.time.LocalDate;

import java.time.LocalDate;
import java.util.List;

public class League {

    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    // Quan hệ 1-N
    private List<Round> rounds;
    private List<LeagueTeam> leagueTeams;

    // Constructor
    public League(Integer id, String name, LocalDate startDate, LocalDate endDate, String description, List<Round> rounds, List<LeagueTeam> leagueTeams) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.rounds = rounds;
        this.leagueTeams = leagueTeams;
    }
    public League() {
    }

    //Getter setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public List<LeagueTeam> getLeagueTeams() {
        return leagueTeams;
    }

    public void setLeagueTeams(List<LeagueTeam> leagueTeams) {
        this.leagueTeams = leagueTeams;
    }
}
