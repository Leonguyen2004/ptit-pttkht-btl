package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.RoundDAO;
import leaguemanager.entity.League;
import leaguemanager.entity.Round;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * HTTP request handler cho các route liên quan đến Round.
 */
public class RoundRouterAPI extends RouterAPI {

    private RoundDAO roundDAO = new RoundDAO();

    /**
     * Constructor: Cấu hình Gson để xử lý LocalDate (giống LeagueRouterAPI)
     */
    public RoundRouterAPI() {
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
        if (method.equals("GET") && path.equals("/api/rounds")) {
            handleGetRounds(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/rounds
     * Supports query params:
     * 1. ?leagueId=1 (Lấy tất cả round của league 1)
     * 2. ?leagueId=1&name=Semi (Tìm round tên 'Semi' trong league 1)
     */
    private void handleGetRounds(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer leagueId = null;
            String nameKeyword = null;

            // Parse query parameters thủ công
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1) {
                        String key = keyValue[0];
                        String value = keyValue[1];

                        if (key.equals("leagueId")) {
                            try {
                                leagueId = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                sendResponse(exchange, 400, "{\"error\": \"Invalid leagueId format\"}");
                                return;
                            }
                        } else if (key.equals("name")) {
                            nameKeyword = java.net.URLDecoder.decode(value, "UTF-8");
                        }
                    }
                }
            }

            // Validate input
            if (leagueId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required parameter: leagueId\"}");
                return;
            }

            // Tạo đối tượng League tạm chỉ chứa ID để truyền vào DAO theo yêu cầu
            League contextLeague = new League();
            contextLeague.setId(leagueId);

            List<Round> rounds;

            // Chọn luồng xử lý dựa trên params
            if (nameKeyword != null && !nameKeyword.isEmpty()) {
                // Case: findRoundByNameAndLeague
                rounds = roundDAO.findRoundByNameAndLeague(nameKeyword, contextLeague);
            } else {
                // Case: findRoundByLeague
                rounds = roundDAO.findRoundByLeague(contextLeague);
            }

            String jsonResponse = gson.toJson(rounds);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in handleGetRounds: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
}