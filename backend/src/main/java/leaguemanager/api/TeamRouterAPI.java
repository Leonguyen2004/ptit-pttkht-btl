package leaguemanager.api;

import com.google.gson.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import leaguemanager.dao.TeamDAO;
import leaguemanager.entity.Stadium;
import leaguemanager.entity.Team;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * HTTP request handler cho các route liên quan đến Team.
 */
public class TeamRouterAPI extends RouterAPI {

    private TeamDAO teamDAO = new TeamDAO();
    private static final String UPLOAD_DIR = "uploads/logos/";

    public TeamRouterAPI() {
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

        // Tạo thư mục upload nếu chưa tồn tại
        new File(UPLOAD_DIR).mkdirs();
    }

    @Override
    protected void handleSpecificRoutes(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        System.out.println("[v0] Request: " + method + " " + path);

        if (method.equals("GET") && path.equals("/api/teams")) {
            getListTeam(exchange);
        } else if (method.equals("POST") && path.equals("/api/teams")) {
            addTeam(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Route not found\"}");
        }
    }

    private void getListTeam(HttpExchange exchange) {
        try {
            List<Team> teams = teamDAO.getListTeam();
            String jsonResponse = gson.toJson(teams);
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void addTeam(HttpExchange exchange) {
        try {

            Headers headers = exchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");
            if (contentType == null) {
                contentType = headers.getFirst("content-type");
            }

            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
                handleMultipartUpload(exchange, contentType);
            } else {
                System.out.println("Handling as JSON because Content-Type is not multipart");
                handleJsonUpload(exchange);
            }

        } catch (Exception e) {
            System.err.println("Error in addTeam handler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleJsonUpload(HttpExchange exchange) throws Exception {
        String requestBody = readRequestBody(exchange);
        if (requestBody.startsWith("--")) {
            sendResponse(exchange, 400, "{\"error\": \"Detected Multipart data but Content-Type header is missing or wrong. Please check your request headers.\"}");
            return;
        }

        try {
            Team newTeam = gson.fromJson(requestBody, Team.class);
            saveTeamToDB(exchange, newTeam);
        } catch (JsonSyntaxException e) {
            System.err.println("[ERROR] JSON Parse Error. Body received: " + requestBody);
            throw e;
        }
    }

    private void handleMultipartUpload(HttpExchange exchange, String contentType) throws IOException {
        String boundaryToken = "boundary=";
        int boundaryIndex = contentType.indexOf(boundaryToken);
        if (boundaryIndex == -1) {
            sendResponse(exchange, 400, "{\"error\": \"Missing boundary in Content-Type\"}");
            return;
        }
        String boundary = contentType.substring(boundaryIndex + boundaryToken.length());
        boundary = boundary.replace("\"", "");

        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] bodyBytes = buffer.toByteArray();

        if (bodyBytes.length == 0) {
            sendResponse(exchange, 400, "{\"error\": \"Request body is empty\"}");
            return;
        }

        Team newTeam = new Team();
        String stadiumIdStr = null;

        String bodyStr = new String(bodyBytes, StandardCharsets.ISO_8859_1);
        String[] parts = bodyStr.split("--" + boundary);

        for (String part : parts) {
            if (part.trim().isEmpty() || part.trim().equals("--")) continue;

            if (part.contains("Content-Disposition: form-data;")) {
                int headerEndIndex = part.indexOf("\r\n\r\n");
                if (headerEndIndex != -1) {
                    String header = part.substring(0, headerEndIndex);

                    // Tính toán vị trí byte
                    int partStartIndexInBytes = indexOf(bodyBytes, part.getBytes(StandardCharsets.ISO_8859_1), 0);
                    // Nếu tìm thấy (có thể cần loop tìm đúng vị trí nếu trùng lặp nội dung, nhưng tạm thời lấy vị trí đầu tiên tìm thấy trong context này)

                    if (partStartIndexInBytes == -1) continue;

                    int contentStartIndex = partStartIndexInBytes + headerEndIndex + 4;
                    int contentEndIndex = partStartIndexInBytes + part.length() - 2; // Trừ \r\n cuối part

                    if (header.contains("filename=")) {
                        // --- XỬ LÝ FILE ẢNH ---
                        String originalFilename = extractValue(header, "filename=\"");
                        if (originalFilename != null && !originalFilename.isEmpty()) {
                            String fileExt = "";
                            int i = originalFilename.lastIndexOf('.');
                            if (i > 0) {
                                fileExt = originalFilename.substring(i);
                            }

                            String newFilename = UUID.randomUUID().toString() + fileExt;
                            Path destination = Paths.get(UPLOAD_DIR + newFilename);

                            // Validate index để tránh lỗi out of bound
                            if (contentEndIndex > contentStartIndex) {
                                byte[] fileData = new byte[contentEndIndex - contentStartIndex];
                                System.arraycopy(bodyBytes, contentStartIndex, fileData, 0, fileData.length);
                                Files.write(destination, fileData, StandardOpenOption.CREATE);
                                newTeam.setLogo(UPLOAD_DIR + newFilename);
                            }
                        }
                    } else {
                        // --- XỬ LÝ TEXT FIELDS ---
                        String name = extractValue(header, "name=\"");
                        if (contentEndIndex > contentStartIndex) {
                            String value = part.substring(headerEndIndex + 4, part.length() - 2);
                            value = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                            if (name != null) {
                                switch (name) {
                                    case "fullName": newTeam.setFullName(value); break;
                                    case "shortName": newTeam.setShortName(value); break;
                                    case "headCoach": newTeam.setHeadCoach(value); break;
                                    case "homeKitColor": newTeam.setHomeKitColor(value); break;
                                    case "awayKitColor": newTeam.setAwayKitColor(value); break;
                                    case "achievements": newTeam.setAchievements(value); break;
                                    case "stadiumId": stadiumIdStr = value; break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (stadiumIdStr != null && !stadiumIdStr.isEmpty()) {
            try {
                Stadium s = new Stadium();
                s.setId(Integer.parseInt(stadiumIdStr));
                newTeam.setStadium(s);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        saveTeamToDB(exchange, newTeam);
    }

    private int indexOf(byte[] outerArray, byte[] smallerArray, int start) {
        for(int i = start; i < outerArray.length - smallerArray.length + 1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    private String extractValue(String header, String key) {
        int start = header.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = header.indexOf("\"", start);
        if (end == -1) return null;
        return header.substring(start, end);
    }

    private void saveTeamToDB(HttpExchange exchange, Team newTeam) {
        if (newTeam.getFullName() == null || newTeam.getFullName().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\": \"Full Name is required\"}");
            return;
        }

        Team savedTeam = teamDAO.addTeam(newTeam);

        if (savedTeam != null) {
            String jsonResponse = gson.toJson(savedTeam);
            sendResponse(exchange, 201, jsonResponse);
        } else {
            sendResponse(exchange, 500, "{\"error\": \"Failed to create team\"}");
        }
    }
}