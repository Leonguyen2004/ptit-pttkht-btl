package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.MatchDAO;
import leaguemanager.entity.League;
import leaguemanager.entity.LeagueTeam;
import leaguemanager.entity.Match;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP request handler cho các route liên quan đến Match.
 */
public class MatchRouterAPI extends RouterAPI {

    private MatchDAO matchDAO = new MatchDAO();

    /**
     * Constructor: Cấu hình Gson cho cả LocalDate và LocalTime
     */
    public MatchRouterAPI() {
        this.gson = new GsonBuilder()
                // Adapter cho LocalDate
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
                // Adapter cho LocalTime (HH:mm:ss)
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

        if (method.equals("GET") && path.equals("/api/matches")) {
            handleGetMatches(exchange);
        } else if (method.equals("POST") && path.equals("/api/matches")) {
            addMatch(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/matches
     * Supports:
     * 1. ?leagueId=X  (findMatchByLeague)
     * 2. ?leagueTeamId=Y (findMatchByLeagueTeam)
     */
    private void handleGetMatches(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer leagueId = null;
            Integer leagueTeamId = null;
            String status = null;

            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1) {
                        if (keyValue[0].equals("leagueId")) {
                            leagueId = Integer.parseInt(keyValue[1]);
                        } else if (keyValue[0].equals("leagueTeamId")) {
                            leagueTeamId = Integer.parseInt(keyValue[1]);
                        } else if (keyValue[0].equals("status")) {
                            status = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        }
                    }
                }
            }

            List<Match> matches;

            if (leagueId != null) {
                League league = new League();
                league.setId(leagueId);
                matches = matchDAO.findMatchByLeague(league, status);
            } else if (leagueTeamId != null) {
                LeagueTeam lt = new LeagueTeam();
                lt.setId(leagueTeamId);
                matches = matchDAO.findMatchByLeagueTeam(lt, status);
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Missing required parameter: leagueId or leagueTeamId\"}");
                return;
            }

            String jsonResponse = gson.toJson(matches);
            sendResponse(exchange, 200, jsonResponse);

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        } catch (Exception e) {
            System.err.println("[v0] Error in handleGetMatches: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Handle POST /api/matches - Thêm trận đấu mới
     */
    private void addMatch(HttpExchange exchange) {
        try {
            String requestBody = readRequestBody(exchange);
            Match newMatch = gson.fromJson(requestBody, Match.class);

            // Validate basic
            if (newMatch.getStadium() == null || newMatch.getStadium().getId() == null) {
                sendResponse(exchange, 400, "{\"error\": \"Stadium ID is required\"}");
                return;
            }

            // DAO sẽ throw exception nếu có conflict
            Match savedMatch = matchDAO.add(newMatch);

            String jsonResponse = gson.toJson(savedMatch);
            sendResponse(exchange, 201, jsonResponse);

        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            // Bắt lỗi Conflict từ DAO để trả về 409 Conflict hoặc 400
            if (e.getMessage().startsWith("Conflict")) {
                sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
            } else {
                System.err.println("[v0] Error adding match: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }
}