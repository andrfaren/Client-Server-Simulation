package server;

import java.nio.file.Path;

public class Main {

    public static void main(String[] strings) {

        // Path to database file
        final Path dbPath = Path.of("./src/main/java/server/data/db.json");

        // Create the db
        final Database jsonDatabase = new Database(dbPath);

        // Create the server
        final Server server = new Server(jsonDatabase);

        server.start();

    }
}