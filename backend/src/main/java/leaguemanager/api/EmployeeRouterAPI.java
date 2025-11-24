package leaguemanager.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.EmployeeDAO;
import leaguemanager.entity.Employee;

/**
 * HTTP request handler cho các route liên quan đến Employee.
 * Kế thừa từ BaseApiHandler để sử dụng các hàm tiện ích chung.
 */
public class EmployeeRouterAPI extends RouterAPI {

    private EmployeeDAO employeeDAO = new EmployeeDAO();

    /**
     * Triển khai phương thức trừu tượng từ BaseApiHandler.
     * Chỉ chứa logic điều hướng (routing) cụ thể cho Employee.
     */
    @Override
    protected void handleSpecificRoutes(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        System.out.println("[v0] Request: " + method + " " + path);

        // Logic điều hướng được giữ nguyên
        if (method.equals("POST") && path.equals("/api/employee/register")) {
            handleRegister(exchange);
        } else if (method.equals("POST") && path.equals("/api/employee/login")) {
            handleLogin(exchange);
        } else if (method.equals("GET") && path.equals("/api/employee/me")) {
            handleGetMe(exchange);
        } else {
            // Sử dụng hàm sendResponse() được kế thừa
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle POST /api/register
     */
    private void handleRegister(HttpExchange exchange) {
        try {
            // Sử dụng readRequestBody() và gson được kế thừa
            String requestBody = readRequestBody(exchange);
            JsonObject jsonBody = gson.fromJson(requestBody, JsonObject.class);

            // Validate required fields
            if (!jsonBody.has("username") || !jsonBody.has("password") || !jsonBody.has("email")) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required fields: username, password, email\"}");
                return;
            }

            Employee emp = new Employee();
            emp.setUsername(jsonBody.get("username").getAsString());
            emp.setPassword(jsonBody.get("password").getAsString());
            emp.setEmail(jsonBody.get("email").getAsString());

            if (jsonBody.has("dateOfBirth")) {
                emp.setDateOfBirth(java.time.LocalDate.parse(jsonBody.get("dateOfBirth").getAsString()));
            }
            if (jsonBody.has("address")) {
                emp.setAddress(jsonBody.get("address").getAsString());
            }
            if (jsonBody.has("phoneNumber")) {
                emp.setPhoneNumber(jsonBody.get("phoneNumber").getAsString());
            }

            if (employeeDAO.register(emp)) {
                JsonObject response = new JsonObject();
                response.addProperty("message", "User registered successfully");
                response.addProperty("username", emp.getUsername());
                sendResponse(exchange, 201, response.toString());
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Registration failed\"}");
            }

        } catch (Exception e) {
            System.err.println("[v0] Error in register handler: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Invalid request body\"}");
        }
    }

    /**
     * Handle POST /api/login
     */
    private void handleLogin(HttpExchange exchange) {
        try {
            String requestBody = readRequestBody(exchange);
            JsonObject jsonBody = gson.fromJson(requestBody, JsonObject.class);

            // Validate required fields
            if (!jsonBody.has("username") || !jsonBody.has("password")) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required fields: username, password\"}");
                return;
            }

            String username = jsonBody.get("username").getAsString();
            String plainPassword = jsonBody.get("password").getAsString();

            Employee emp = employeeDAO.login(username, plainPassword);

            if (emp != null) {
                JsonObject response = new JsonObject();
                response.addProperty("employeeId", emp.getId());
                response.addProperty("username", emp.getUsername());
                response.addProperty("email", emp.getEmail());
                response.addProperty("address", emp.getAddress());
                response.addProperty("phoneNumber", emp.getPhoneNumber());
                if (emp.getDateOfBirth() != null) {
                    response.addProperty("dateOfBirth", emp.getDateOfBirth().toString());
                }

                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 401, "{\"error\": \"Invalid username or password\"}");
            }

        } catch (Exception e) {
            System.err.println("[v0] Error in login handler: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Invalid request body\"}");
        }
    }

    /**
     * Handle GET /api/employee/me
     * Lấy thông tin employee từ employeeId trong query parameter
     */
    private void handleGetMe(HttpExchange exchange) {
        try {
            // Lấy employeeId từ query parameter
            String query = exchange.getRequestURI().getQuery();
            String employeeId = null;
            
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("employeeId")) {
                        employeeId = keyValue[1];
                        break;
                    }
                }
            }

            if (employeeId == null || employeeId.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing employeeId parameter\"}");
                return;
            }

            // Get employee details
            Employee emp = employeeDAO.getById(Integer.parseInt(employeeId));

            if (emp != null) {
                JsonObject response = new JsonObject();
                response.addProperty("id", emp.getId());
                response.addProperty("username", emp.getUsername());
                response.addProperty("email", emp.getEmail());
                response.addProperty("address", emp.getAddress());
                response.addProperty("phoneNumber", emp.getPhoneNumber());
                if (emp.getDateOfBirth() != null) {
                    response.addProperty("dateOfBirth", emp.getDateOfBirth().toString());
                }

                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Employee not found\"}");
            }

        } catch (NumberFormatException e) {
            System.err.println("[v0] Invalid employee ID format");
            sendResponse(exchange, 400, "{\"error\": \"Invalid employee ID format\"}");
        } catch (Exception e) {
            System.err.println("[v0] Error in /api/employee/me handler: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

//    private Employee handleGetInfor() {
//
//    }
}