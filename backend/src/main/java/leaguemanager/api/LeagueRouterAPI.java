package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.LeagueDAO;
import leaguemanager.entity.League;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP request handler cho các route liên quan đến League.
 */
public class LeagueRouterAPI extends RouterAPI {

    private LeagueDAO leagueDAO = new LeagueDAO();

    /**
     * Constructor này cấu hình lại Gson để xử lý LocalDate.
     * Nếu không có đoạn này, Gson sẽ không parse được field StartDate/EndDate.
     */
    public LeagueRouterAPI() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-MM-dd"
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
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME)); // "HH:mm:ss"
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
        if (method.equals("GET") && path.equals("/api/leagues")) {
            handleFindLeagueByName(exchange);
        } else if (method.equals("POST") && path.equals("/api/leagues")) {
            addLeague(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/leagues?name=Premier
     * Tìm kiếm giải đấu theo tên
     */
    private void handleFindLeagueByName(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            String nameKeyword = "";

            if (query != null && query.contains("name=")) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("name")) {
                        nameKeyword = keyValue[1];
                        break;
                    }
                }
            }

            nameKeyword = java.net.URLDecoder.decode(nameKeyword, "UTF-8");

            List<League> leagues = leagueDAO.findLeagueByName(nameKeyword);

            String jsonResponse = gson.toJson(leagues);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in findLeagueByName handler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Handle POST /api/leagues
     * Thêm mới giải đấu
     * JSON Body sample: {"name": "V-League", "startDate": "2023-01-01", "endDate": "2023-12-31", "description": "Vietnam League"}
     */
    private void addLeague(HttpExchange exchange) {
        try {
            String requestBody = readRequestBody(exchange);

            // Gson đã được cấu hình ở constructor để parse chuỗi "yyyy-MM-dd" thành LocalDate
            League newLeague = gson.fromJson(requestBody, League.class);

            if (newLeague.getName() == null || newLeague.getName().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"League Name is required\"}");
                return;
            }

            League savedLeague = leagueDAO.add(newLeague);

            if (savedLeague != null) {
                String jsonResponse = gson.toJson(savedLeague);
                sendResponse(exchange, 201, jsonResponse);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create league\"}");
            }

        } catch (Exception e) {
            System.err.println("[v0] Error in addLeague handler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
}