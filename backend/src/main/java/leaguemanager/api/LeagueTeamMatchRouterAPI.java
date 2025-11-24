package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.LeagueTeamMatchDAO;
import leaguemanager.entity.LeagueTeamMatch;
import leaguemanager.entity.Match;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP request handler cho các route liên quan đến LeagueTeamMatch.
 */
public class LeagueTeamMatchRouterAPI extends RouterAPI {

    private LeagueTeamMatchDAO leagueTeamMatchDAO = new LeagueTeamMatchDAO();

    /**
     * Constructor: Cấu hình Gson để xử lý LocalDate/LocalTime
     * (Phòng trường hợp entity Match được serialize đầy đủ trong tương lai)
     */
    public LeagueTeamMatchRouterAPI() {
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

        // GET /api/league-team-matches?matchId=...
        if (method.equals("GET") && path.equals("/api/league-team-matches")) {
            handleGetByMatch(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/league-team-matches?matchId=1
     * Lấy danh sách các đội tham gia trận đấu cụ thể (thường là 2 đội)
     */
    private void handleGetByMatch(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer matchId = null;

            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("matchId")) {
                        try {
                            matchId = Integer.parseInt(keyValue[1]);
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, 400, "{\"error\": \"Invalid matchId format\"}");
                            return;
                        }
                    }
                }
            }

            if (matchId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required parameter: matchId\"}");
                return;
            }

            // Tạo đối tượng Match tạm
            Match match = new Match();
            match.setId(matchId);

            List<LeagueTeamMatch> results = leagueTeamMatchDAO.getLeagueTeamMatchByMatch(match);

            String jsonResponse = gson.toJson(results);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in handleGetByMatch: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
}