package server;

import java.nio.file.Path;

public class Database {
    final private Path dbPath;
    public Database(Path dbPath) {
        this.dbPath = dbPath;
    }


    // Get methods
    public String getValue(String key) {

        return "";
    }
    public String getValue(String[] keys) {

        return "";
    }

    public Path getDbPath() {
        return dbPath;
    }

    // Set

    // Delete

}
