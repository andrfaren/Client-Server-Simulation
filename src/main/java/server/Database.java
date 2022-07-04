package server;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Database {

    private final Path dbPath;
    private Map<String, String> dbMap = new HashMap<>();

    public Database(Path dbPath) {
        this.dbPath = dbPath;
    }

    public Path getDbPath() {
        return this.dbPath;
    }

    public void setAndWriteToDb(String key, String value) {

        dbMap = loadHashMap(dbPath);

        // Update the HashMap
        dbMap.put(key, value);

        String stringToWriteToDb = new Gson().toJson(dbMap);

        writeToDb(stringToWriteToDb);

    }

    public void deleteAndWriteToDb(String key) {

        dbMap = loadHashMap(dbPath);

        // Update the HashMap
        dbMap.remove(key);

        String stringToWriteToDb = new Gson().toJson(dbMap);

        writeToDb(stringToWriteToDb);

    }

    public void writeToDb(String jsonString) {
        // Write to db.json
        try {
            Files.writeString(dbPath, jsonString);
        } catch (IOException e) {
            System.out.println("Problem reading database file.");
        }
    }

    public Map<?, ?> getDbMap() {
        return this.dbMap;
    }

    public String getValue(String key) {

        return dbMap.get(key);
    }

    // This method generates a HashMap from a file containing JSON data
    public static Map<String, String> loadHashMap(Path dbFile) {
        try {
            String dbString = Files.readString(dbFile);
            if (dbString != null && !dbString.equals("")) {
                Map<String, String> dbMap = new Gson().fromJson(dbString, HashMap.class);

                return dbMap;

            } else return new HashMap<>();
        } catch (IOException e) {
            System.out.println("Problem reading database from file.");

            return null;
        }
    }

    private static void writeToDatabase(Path dbPath, String entry) {
        // Write to db.json
        try {
            Files.writeString(dbPath, entry);
        } catch (IOException e) {
            System.out.println("Problem reading database file.");
        }

    }

}
