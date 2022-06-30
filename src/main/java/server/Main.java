package server;

import java.nio.file.Path;

public class Main {

    public static void main(String[] strings) {

        // Path to database file
        final Path dbPath = Path.of("C:\\Users\\andre\\Documents\\repos\\Client-Server-Simulation\\src\\main\\java\\server\\data\\db.json");

        // Create the db
        final Database jsonDatabase = new Database(dbPath);

        // Create the server
        final Server server = new Server(jsonDatabase);

        server.start();

    }
}

//        final Path dbPath = Path.of("./src/server/data/db.json");

//        System.out.println(Files.exists(dbPath));
//        System.out.println(System.getProperty("user.dir"));
//    final Path dbPath = Path.of("C:\\Users\\andre\\IdeaProjects\\JSON Database\\task\\src\\server\\data\\db.json");