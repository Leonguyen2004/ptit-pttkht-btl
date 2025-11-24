package leaguemanager.entity;

import java.time.LocalDate;
import java.util.List;

public class Round {

    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    // Quan hệ N-1
    private League league;
    // Quan hệ 1-N
    private List<Match> matches;

    // ---Constructors ---
    public Round(Integer id, String name, LocalDate startDate, LocalDate endDate, String description, League league, List<Match> matches) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.league = league;
        this.matches = matches;
    }

    public Round() {
    }

    //getter setter
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

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
