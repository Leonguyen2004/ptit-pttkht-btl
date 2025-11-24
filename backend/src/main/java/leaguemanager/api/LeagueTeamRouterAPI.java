package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.LeagueTeamDAO;
import leaguemanager.entity.LeagueTeam;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP request handler cho các route liên quan đến LeagueTeam.
 */
public class LeagueTeamRouterAPI extends RouterAPI {

    private LeagueTeamDAO leagueTeamDAO = new LeagueTeamDAO();

    /**
     * Constructor: Cấu hình Gson để xử lý LocalDate (đề phòng League object có chứa date)
     */
    public LeagueTeamRouterAPI() {
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

        if (method.equals("GET") && path.equals("/api/league-teams")) {
            handleGetLeagueTeams(exchange);
        } else if (method.equals("POST") && path.equals("/api/league-teams")) {
            addLeagueTeam(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    /**
     * Handle GET /api/league-teams
     * Supports:
     * 1. No params: Get All
     * 2. ?name=Man: Search by Team Name
     */
    private void handleGetLeagueTeams(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            String nameKeyword = null;

            if (query != null && query.contains("name=")) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("name")) {
                        nameKeyword = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                        break;
                    }
                }
            }

            List<LeagueTeam> results;
            if (nameKeyword != null && !nameKeyword.isEmpty()) {
                results = leagueTeamDAO.findByName(nameKeyword);
            } else {
                results = leagueTeamDAO.getAll();
            }

            String jsonResponse = gson.toJson(results);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("[v0] Error in handleGetLeagueTeams: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Handle POST /api/league-teams
     * Register a team to a league
     */
    private void addLeagueTeam(HttpExchange exchange) {
        try {
            String requestBody = readRequestBody(exchange);
            LeagueTeam newLT = gson.fromJson(requestBody, LeagueTeam.class);

            LeagueTeam savedLT = leagueTeamDAO.add(newLT);

            if (savedLT != null) {
                String jsonResponse = gson.toJson(savedLT);
                sendResponse(exchange, 201, jsonResponse);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to register team to league\"}");
            }

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("already registered")) {
                sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
            } else {
                System.err.println("[v0] Error in addLeagueTeam: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Internal server error or Invalid Data\"}");
            }
        }
    }
}