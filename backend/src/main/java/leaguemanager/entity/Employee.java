package leaguemanager.entity;

import java.time.LocalDate;

public class Employee {

    private Integer id;
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String phoneNumber;

    // Quan hệ 1-1
    private Staff staff;

    // Constructor
    public Employee(Integer id, String username, String password, LocalDate dateOfBirth, String address, String email, String phoneNumber, Staff staff) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.staff = staff;
    }
    public Employee() {
    }

    //Getter Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }
}
