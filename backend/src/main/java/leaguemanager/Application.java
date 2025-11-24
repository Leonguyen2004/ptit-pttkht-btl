package leaguemanager;

import com.sun.net.httpserver.HttpServer;
import leaguemanager.api.*;

import java.net.InetSocketAddress;

public class Application {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            // 1. Quản lý nhân viên (Employee)
            server.createContext("/api/employee", new EmployeeRouterAPI());

            // 2. Quản lý đội bóng (Team)
            server.createContext("/api/teams", new TeamRouterAPI());

            // 3. Quản lý giải đấu (League)
            server.createContext("/api/leagues", new LeagueRouterAPI());

            // 4. Quản lý vòng đấu (Round)
            server.createContext("/api/rounds", new RoundRouterAPI());

            // 5. Quản lý sân vận động (Stadium)
            server.createContext("/api/stadiums", new StadiumRouterAPI());

            // 6. Quản lý đội tham gia giải (LeagueTeam)
            server.createContext("/api/league-teams", new LeagueTeamRouterAPI());

            // 7. Quản lý trận đấu (Match)
            server.createContext("/api/matches", new MatchRouterAPI());

            // 8. Xem bảng xếp hạng (Ranking)
            server.createContext("/api/rankings", new RankingRouterAPI());

            // 9. LeagueTEamMacth
            server.createContext("/api/league-team-matches", new LeagueTeamMatchRouterAPI());

            // 10. Static files (uploads)
            server.createContext("/uploads", new leaguemanager.api.StaticFileHandler());

            // Start the server with default executor
            server.setExecutor(null);
            server.start();

            System.out.println("[v0] Server started on port " + PORT);

        } catch (Exception e) {
            System.err.println("[v0] Failed to start server");
            e.printStackTrace();
        }
    }
}
