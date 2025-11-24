package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.StadiumDAO;
import leaguemanager.entity.Stadium;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * HTTP request handler cho các route liên quan đến Stadium.
 */
public class StadiumRouterAPI extends RouterAPI {

    private StadiumDAO stadiumDAO = new StadiumDAO();

    public StadiumRouterAPI() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                })
                .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
                    @Override
                    public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME));
                    }
                })
                .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
                    @Override
                    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
                    }
                })
                .create();
    }

    @Override
    protected void handleSpecificRoutes(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        System.out.println("[v0] Request: " + method + " " + path);

        // Logic điều hướng
        if (method.equals("GET") && path.equals("/api/stadiums")) {
            handleFindStadiumByName(exchange);
        } else if (method.equals("POST") && path.equals("/api/stadiums")) {
            addStadium(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/stadiums?name=MyStadium
     * Tìm kiếm sân vận động theo tên
     */
    private void handleFindStadiumByName(HttpExchange exchange) {
        try {
            // Lấy query query (ví dụ: name=MyStadium)
            String query = exchange.getRequestURI().getQuery();
            String nameKeyword = "";

            if (query != null && query.contains("name=")) {
                // Parse đơn giản để lấy giá trị name
                // Để robust hơn nên dùng thư viện parse query string, nhưng ở đây làm thủ công cho gọn
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("name")) {
                        nameKeyword = keyValue[1];
                        break; // Lấy giá trị name đầu tiên tìm thấy
                    }
                }
            }

            // Decode URL parameter nếu cần (ví dụ thay %20 bằng space)
            nameKeyword = java.net.URLDecoder.decode(nameKeyword, "UTF-8");

            List<Stadium> stadiums = stadiumDAO.findStadiumByName(nameKeyword);

            String jsonResponse = gson.toJson(stadiums);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in findStadiumByName handler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Handle POST /api/stadiums
     * Thêm mới sân vận động
     */
    private void addStadium(HttpExchange exchange) {
        try {
            // 1. Đọc request body
            String requestBody = readRequestBody(exchange);

            // 2. Parse JSON
            Stadium newStadium = gson.fromJson(requestBody, Stadium.class);

            // Validate
            if (newStadium.getName() == null || newStadium.getName().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Stadium Name is required\"}");
                return;
            }

            // 3. Gọi DAO
            Stadium savedStadium = stadiumDAO.addStadium(newStadium);

            if (savedStadium != null) {
                String jsonResponse = gson.toJson(savedStadium);
                sendResponse(exchange, 201, jsonResponse);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create stadium\"}");
            }

        } catch (Exception e) {
            System.err.println("[v0] Error in addStadium handler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
}