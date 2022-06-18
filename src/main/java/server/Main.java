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
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    public static void main(String[] args) {

        // References to database files
//        final Path dbPath = Path.of("./JSON Database/task/src/server/data/db.json");
        final Path dbPath = Path.of("./src/server/data/db.json");
//        System.out.println(Files.exists(dbPath));
//        System.out.println(System.getProperty("user.dir"));
//    final Path dbPath = Path.of("C:\\Users\\andre\\IdeaProjects\\JSON Database\\task\\src\\server\\data\\db.json");

        // Server's IP address
        String ipAddress = "127.0.0.1";

        // Port to be used for communication with a client
        int port = 23456;

        boolean running = true;

        try {
            System.out.println("Server started!");

            while (running) {
                try {
                    // Create a server socket that will accept or decline requests
                    ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(ipAddress));

                    // Listen for requests
                    Socket socket = server.accept();

                    DataInputStream input = new DataInputStream(socket.getInputStream());

                    // Prepare an output stream for sending response codes ("OK", "ERROR")
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                    // Read message from the client in JSON
                    String receivedMessage = input.readUTF(); // Example: {"type":"set","key":"1","value":"HelloWorld!"}

                    Args userArgs = new Gson().fromJson(receivedMessage, Args.class);

                    // Create an executor
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
                                    // HashMap used to load db file contents
                                    HashMap<String, String> dbMap = null;

                                    dbMap = generateHashMap(dbPath);

                                    // Update the HashMap
                                    dbMap.put(userArgs.key, userArgs.value);

                                    // Write to db.json
                                    Files.writeString(dbPath, new Gson().toJson(dbMap));
                                    writeLock.unlock();

                                    // Send response
                                    String JSONResponseSetOK = new Gson().toJson(new Response(ResponseType.OK));
                                    output.writeUTF(JSONResponseSetOK);

                                    output.close();
                                    socket.close();
                                    server.close();

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

                                    dbMap = generateHashMap(dbPath);

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

                                    output.close();
                                    socket.close();
                                    server.close();

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            break;

                        case delete:
                            executor.submit(() -> {
                                writeLock.lock();

                                try {
                                    // HashMap used to load db file contents
                                    HashMap<String, String> dbMap = null;

                                    dbMap = generateHashMap(dbPath);

                                    System.out.println("Before delete" + dbMap.toString());

                                    if (dbMap.containsKey(userArgs.key)) {

                                        // Update the HashMap
                                        dbMap.remove(userArgs.key);

                                        // Write to db.json
                                        Files.writeString(dbPath, new Gson().toJson(dbMap));

                                        writeLock.unlock();

                                        System.out.println("After delete" + dbMap);
                                        String JSONResponseDeleteOK = new Gson().toJson(new Response(ResponseType.OK));
                                        output.writeUTF(JSONResponseDeleteOK);

                                    } else {
                                        String JSONResponseDeleteERROR = new Gson().toJson(new Response(ResponseType.ERROR, null, "No such key"));
                                        output.writeUTF(JSONResponseDeleteERROR);
                                    }

                                    output.close();
                                    socket.close();
                                    server.close();

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            break;

                        case exit:
                            running = false;
                            executor.submit(() -> {

                                String JSONResponseExitOK = new Gson().toJson(new Response(ResponseType.OK));
                                try {
                                    output.writeUTF(JSONResponseExitOK);

                                    output.close();
                                    socket.close();
                                    server.close();
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
            throw new RuntimeException(e);
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
}
