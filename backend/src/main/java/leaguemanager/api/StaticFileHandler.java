package leaguemanager.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handler để serve static files từ thư mục uploads
 */
public class StaticFileHandler implements HttpHandler {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Loại bỏ prefix /uploads/ để lấy đường dẫn file
        String filePath = path.replaceFirst("^/uploads/", "");
        Path file = Paths.get(UPLOAD_DIR + filePath);

        // Kiểm tra file có tồn tại và nằm trong thư mục uploads
        if (!Files.exists(file) || !file.normalize().startsWith(Paths.get(UPLOAD_DIR).normalize())) {
            send404(exchange);
            return;
        }

        // Xác định Content-Type dựa trên extension
        String contentType = getContentType(file.toString());
        
        // Đọc file và gửi response
        byte[] fileData = Files.readAllBytes(file);
        
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Access-Control-Allow-Origin", "*");
        
        exchange.sendResponseHeaders(200, fileData.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileData);
        os.close();
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    private void send404(HttpExchange exchange) throws IOException {
        String response = "File not found";
        exchange.sendResponseHeaders(404, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

