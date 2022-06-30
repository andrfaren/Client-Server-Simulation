package server;

import com.google.gson.Gson;
import server.response.Response;
import server.response.ResponseType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    private final Database jsonDatabase;

    public Server(Database jsonDatabase) {
        this.jsonDatabase = jsonDatabase;

    }

    public void start() {

        boolean isRunning = true;

        try {
            System.out.println("Server started!");

            while (isRunning) {
                try     (
                        ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT, 50, InetAddress.getByName(ServerConfig.IP_ADDRESS));
                        Socket socket = serverSocket.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                        )

                {
                    String receivedMessage = input.readUTF(); // Example: {"type":"set","key":"1","value":"HelloWorld!"}

                    Args userArgs = new Gson().fromJson(receivedMessage, Args.class);

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    // Locks
                    ReadWriteLock lock = new ReentrantReadWriteLock();
                    Lock writeLock = lock.writeLock();
                    Lock readLock = lock.readLock();

                    switch (userArgs.type) {

                        case set:

                            executor.submit(() -> {
                                writeLock.lock();

                                try {

                                    if (userArgs.inputFile != null) {

                                        writeToDatabase(jsonDatabase.getDbPath(), receivedMessage);

                                    } else {
                                        // HashMap used to load db file contents
                                        HashMap<String, String> dbMap = null;

                                        dbMap = generateHashMap(jsonDatabase.getDbPath());

                                        // Update the HashMap
                                        dbMap.put(userArgs.key, userArgs.value);

                                        // Write to db.json
                                        Files.writeString(jsonDatabase.getDbPath(), new Gson().toJson(dbMap));

                                    }

                                    writeLock.unlock();

                                    // Send response
                                    String JSONResponseSetOK = new Gson().toJson(new Response(ResponseType.OK));
                                    output.writeUTF(JSONResponseSetOK);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            break;

                        case get:
                            executor.submit(() -> {
                                        readLock.lock();

                                        try {
                                            // HashMap used to load db file contents
                                            HashMap<String, String> dbMap = null;

                                            dbMap = generateHashMap(jsonDatabase.getDbPath());

                                            if (dbMap.containsKey(userArgs.key)) {

                                                String retrievedValue = dbMap.get(userArgs.key);
                                                readLock.unlock();

                                                String JSONResponseGetOK = new Gson().toJson(new Response(ResponseType.OK, retrievedValue, null));
                                                output.writeUTF(JSONResponseGetOK);

                                                // If the given key was not found, send an ERROR response
                                            } else {
                                                String JSONResponseGetERROR = new Gson().toJson(new Response(ResponseType.ERROR, null, "No such key"));
                                                output.writeUTF(JSONResponseGetERROR);
                                            }

                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );

                            break;

                        case delete:
                            executor.submit(() -> {
                                writeLock.lock();

                                try {
                                    // HashMap used to load db file contents
                                    HashMap<String, String> dbMap = null;

                                    dbMap = generateHashMap(jsonDatabase.getDbPath());

                                    if (dbMap.containsKey(userArgs.key)) {

                                        // Update the HashMap
                                        dbMap.remove(userArgs.key);

                                        // Write to db.json
                                        Files.writeString(jsonDatabase.getDbPath(), new Gson().toJson(dbMap));

                                        writeLock.unlock();

                                        String JSONResponseDeleteOK = new Gson().toJson(new Response(ResponseType.OK));
                                        output.writeUTF(JSONResponseDeleteOK);

                                    } else {
                                        String JSONResponseDeleteERROR = new Gson().toJson(new Response(ResponseType.ERROR, null, "No such key"));
                                        output.writeUTF(JSONResponseDeleteERROR);
                                    }

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            break;

                        case exit:
                            isRunning = false;
                            executor.submit(() -> {

                                String JSONResponseExitOK = new Gson().toJson(new Response(ResponseType.OK));
                                try {
                                    output.writeUTF(JSONResponseExitOK);

                                    // Wait one second before shutting down
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    System.exit(0);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            break;
                    }

                    executor.shutdown();
                    executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

                } catch (IOException e) {

                }
            }
        } catch (Exception e) {

        }
    }

    // This method generates a HashMap from a file containing JSON data
    public static HashMap<String, String> generateHashMap(Path dbFile) throws IOException {
        String dbString = Files.readString(dbFile);
        if (dbString != null && !dbString.equals("")) {
            HashMap<String, String> dbMap = new Gson().fromJson(dbString, HashMap.class);
            return dbMap;

        } else return new HashMap<>();

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
