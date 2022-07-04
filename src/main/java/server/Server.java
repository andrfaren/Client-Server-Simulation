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

    private final Database database;

    public Server(Database database) {
        this.database = database;

    }

    public void start() {

        boolean isRunning = true;

        try {
            System.out.println("Server started!");

            while (isRunning) {
                try (
                        ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT, 50, InetAddress.getByName(ServerConfig.IP_ADDRESS));
                        Socket socket = serverSocket.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
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

                                String stringToWriteToDb = "";

                                if (userArgs.inputFile == null) {

                                    // HashMap used to load db file contents
                                    HashMap<String, String> dbMap = null;

                                    dbMap = generateHashMap(database.dbPath());

                                    // Update the HashMap
                                    dbMap.put(userArgs.key, userArgs.value);

                                    stringToWriteToDb = new Gson().toJson(dbMap);

                                } else {
                                    stringToWriteToDb = receivedMessage;
                                }

                                // Write to database
                                writeToDatabase(database.dbPath(), stringToWriteToDb);

                                writeLock.unlock();

                                // Send response from server to client indicating that 'set' operation was successful
                                sendResponse(Response.setOk(), output);

                            });

                            break;

                        case get:
                            executor.submit(() -> {
                                        readLock.lock();

                                        // HashMap used to load db file contents
                                        HashMap<String, String> dbMap = null;

                                        dbMap = generateHashMap(database.dbPath());

                                        if (dbMap.containsKey(userArgs.key)) {

                                            String retrievedValue = dbMap.get(userArgs.key);
                                            readLock.unlock();

                                            // Send response from server to client indicating that 'get' operation was successful
                                            sendResponse(Response.getOk(retrievedValue), output);

                                        } else {
                                            // If the given key was not found, send an ERROR response
                                            sendResponse(Response.getError("No such key"), output);
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

                                    dbMap = generateHashMap(database.dbPath());

                                    if (dbMap.containsKey(userArgs.key)) {

                                        // Update the HashMap
                                        dbMap.remove(userArgs.key);

                                        // Write to db.json
                                        Files.writeString(database.dbPath(), new Gson().toJson(dbMap));

                                        writeLock.unlock();

                                        // Send response from server to client indicating that 'delete' operation was successful
                                        sendResponse(Response.deleteOk(), output);


                                    } else {
                                        // If the given key was not found, send an ERROR response
                                        sendResponse(Response.deleteError("No such key"), output);
                                    }

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            break;

                        case exit:
                            isRunning = false;
                            executor.submit(() -> {

                                // Send response from server to client indicating that 'exit' operation was successful
                                sendResponse(Response.exitOk(), output);

                                // Wait one second before shutting down
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                System.exit(0);


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

    private void sendResponse(Response response, DataOutputStream output) {

        String JSONResponse = new Gson().toJson(response);

        try {
            output.writeUTF(JSONResponse);
        } catch (IOException e) {
            System.out.println("Problem sending response back to client.");
        }

    }

    // This method generates a HashMap from a file containing JSON data
    public static HashMap<String, String> generateHashMap(Path dbFile) {
        try {
            String dbString = Files.readString(dbFile);
            if (dbString != null && !dbString.equals("")) {
                HashMap<String, String> dbMap = new Gson().fromJson(dbString, HashMap.class);

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
