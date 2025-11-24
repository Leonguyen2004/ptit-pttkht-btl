package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.RankingDAO;
import leaguemanager.entity.League;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP request handler cho các route liên quan đến Ranking.
 */
public class RankingRouterAPI extends RouterAPI {

    private RankingDAO rankingDAO = new RankingDAO();

    /**
     * Constructor: Cấu hình Gson để xử lý LocalDate.
     * (Vì Ranking chứa LeagueTeam -> chứa League -> chứa LocalDate)
     */
    public RankingRouterAPI() {
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

        // Route: GET /api/rankings?leagueId=...
        if (method.equals("GET") && path.equals("/api/rankings")) {
            handleGetRankingByLeague(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/rankings?leagueId=1
     * Lấy bảng xếp hạng của giải đấu
     */
    private void handleGetRankingByLeague(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer leagueId = null;

            // Parse query string để lấy leagueId
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("leagueId")) {
                        try {
                            leagueId = Integer.parseInt(keyValue[1]);
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, 400, "{\"error\": \"Invalid leagueId format\"}");
                            return;
                        }
                    }
                }
            }

            if (leagueId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required parameter: leagueId\"}");
                return;
            }

            // Tạo đối tượng League tạm để truyền vào DAO
            League league = new League();
            league.setId(leagueId);

            // Lấy danh sách Ranking
            List<leaguemanager.dto.RankingDTO> rankings = rankingDAO.getListRankingByLeague(league);

            String jsonResponse = gson.toJson(rankings);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in handleGetRankingByLeague: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
}