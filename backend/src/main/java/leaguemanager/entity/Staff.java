package leaguemanager.entity;

public class Staff {

    private Integer id;
    private String role;

    // Quan hệ 1-1
    private Employee employee;

    // Constructors ---
    public Staff(Integer id, String role, Employee employee) {
        this.id = id;
        this.role = role;
        this.employee = employee;
    }

    public Staff() {
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
