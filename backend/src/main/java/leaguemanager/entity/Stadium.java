package leaguemanager.entity;

import java.util.List;

public class Stadium {

    private Integer id;
    private String name;
    private String address;
    private Integer capacity;

    // Quan hệ 1-N
    private List<Team> teams;
    private List<Match> matches;

    // Constructors ---
    public Stadium(Integer id, String name, String address, Integer capacity, List<Team> teams, List<Match> matches) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.teams = teams;
        this.matches = matches;
    }

    public Stadium() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
