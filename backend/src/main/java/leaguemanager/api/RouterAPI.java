package leaguemanager.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lớp trừu tượng cơ sở cho tất cả các API handler.
 * Cung cấp các phương thức tiện ích chung như gửi phản hồi, đọc request body,
 * và xử lý lỗi cơ bản.
 */
public abstract class RouterAPI implements HttpHandler {

    /**
     * Một instance Gson chung để các lớp con sử dụng cho việc parse/serialize JSON.
     * Được đánh dấu là 'protected' để lớp con có thể truy cập.
     */
    protected Gson gson = new Gson();

    /**
     * Phương thức 'handle' chính từ HttpHandler.
     * Nó bao bọc logic của lớp con trong một khối try-catch chung
     * để xử lý các lỗi không mong muốn (Internal Server Error).
     */
    @Override
    public void handle(HttpExchange exchange) {
        try {
            // Cho phép CORS cho các request OPTIONS (pre-flight)
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                handleOptionsRequest(exchange);
            } else {
                // Ủy quyền xử lý các route cụ thể cho lớp con
                handleSpecificRoutes(exchange);
            }
        } catch (Exception e) {
            System.err.println("[v0] UNHANDLED Error handling request: " + e.getMessage());
            e.printStackTrace();
            // Phản hồi lỗi chung
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Các lớp con BẮT BUỘC phải triển khai (implement) phương thức này
     * để định nghĩa các route (đường dẫn) API của riêng chúng.
     *
     * @param exchange Đối tượng HttpExchange
     * @throws Exception
     */
    protected abstract void handleSpecificRoutes(HttpExchange exchange) throws Exception;

    /**
     * Tiện ích chung: Đọc nội dung (body) của một request.
     */
    protected String readRequestBody(HttpExchange exchange) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        reader.close();
        return body.toString();
    }

    /**
     * Tiện ích chung: Gửi phản hồi (response) về cho client.
     * Đã bao gồm các header CORS chung.
     */
    protected void sendResponse(HttpExchange exchange, int statusCode, String responseBody) {
        try {
            // Thiết lập các header CORS chung
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();

            System.out.println("[v0] Response: " + statusCode + " " + responseBody);

        } catch (Exception e) {
            System.err.println("[v0] Error sending response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý các request OPTIONS (pre-flight) của CORS.
     */
    private void handleOptionsRequest(HttpExchange exchange) {
        try {
            // Set CORS headers cho preflight request
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
            
            // Send 204 No Content response for OPTIONS request
            // -1 means no response body
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            
            System.out.println("[v0] OPTIONS preflight request handled for: " + exchange.getRequestURI().getPath());
            
        } catch (Exception e) {
            System.err.println("[v0] Error handling OPTIONS request: " + e.getMessage());
            e.printStackTrace();
        }
    }

}