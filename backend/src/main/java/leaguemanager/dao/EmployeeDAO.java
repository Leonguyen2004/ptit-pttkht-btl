package leaguemanager.dao;

import leaguemanager.entity.Employee;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * DAO for Employee entity - handles registration and login
 */
public class EmployeeDAO extends DAO {

    /**
     * Register a new employee
     * @param emp Employee object containing username, password (plain text), email, etc.
     * @return true if registration successful, false otherwise
     */
    public boolean register(Employee emp) {
        try {
            // Hash the password using BCrypt
            String passwordHash = BCrypt.hashpw(emp.getPassword(), BCrypt.gensalt(10));

            String sql = "INSERT INTO employee (username, password, email, date_of_birth, address, phone_number) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = dbCon.prepareStatement(sql);

            stmt.setString(1, emp.getUsername());
            stmt.setString(2, passwordHash);
            stmt.setString(3, emp.getEmail());
            stmt.setObject(4, emp.getDateOfBirth());
            stmt.setString(5, emp.getAddress());
            stmt.setString(6, emp.getPhoneNumber());

            int rowsInserted = stmt.executeUpdate();
            System.out.println("[v0] Employee registered: " + emp.getUsername());

            stmt.close();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("[v0] Error during registration");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Login an employee
     * @param username Username of the employee
     * @param plainPassword Plain text password to verify
     * @return Employee object if login successful, null otherwise
     */
    public Employee login(String username, String plainPassword) {
        try {
            String sql = "SELECT id, username, password, email, date_of_birth, address, phone_number FROM employee WHERE username = ?";
            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");

                // Verify password using BCrypt
                if (BCrypt.checkpw(plainPassword, storedPasswordHash)) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setUsername(rs.getString("username"));
                    emp.setEmail(rs.getString("email"));
                    emp.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
                    emp.setAddress(rs.getString("address"));
                    emp.setPhoneNumber(rs.getString("phone_number"));

                    System.out.println("[v0] Employee logged in: " + username);
                    stmt.close();
                    return emp;
                } else {
                    System.err.println("[v0] Invalid password for user: " + username);
                }
            } else {
                System.err.println("[v0] User not found: " + username);
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error during login");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get employee by ID
     * @param employeeId ID of the employee
     * @return Employee object if found, null otherwise
     */
    public Employee getById(Integer employeeId) {
        try {
            String sql = "SELECT id, username, email, date_of_birth, address, phone_number FROM employee WHERE id = ?";
            PreparedStatement stmt = dbCon.prepareStatement(sql);
            stmt.setInt(1, employeeId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setUsername(rs.getString("username"));
                emp.setEmail(rs.getString("email"));
                emp.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
                emp.setAddress(rs.getString("address"));
                emp.setPhoneNumber(rs.getString("phone_number"));

                stmt.close();
                return emp;
            }

            stmt.close();
            return null;

        } catch (SQLException e) {
            System.err.println("[v0] Error fetching employee");
            e.printStackTrace();
            return null;
        }
    }
}
