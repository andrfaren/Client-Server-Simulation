package server;

import com.google.gson.Gson;
import server.response.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    private Database database;

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
                                    database.setAndWriteToDb(userArgs.key, userArgs.value);

                                } else {
                                    stringToWriteToDb = receivedMessage;
                                    database.writeToDb(stringToWriteToDb);
                                }

                                writeLock.unlock();

                                // Send response from server to client indicating that 'set' operation was successful
                                sendResponse(Response.setOk(), output);

                            });

                            break;

                        case get:
                            executor.submit(() -> {
                                        readLock.lock();

                                        if (database.getDbMap().containsKey(userArgs.key)) {

                                            String retrievedValue = database.getValue(userArgs.key);
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

                                if (database.getDbMap().containsKey(userArgs.key)) {

                                    // Remove the value with the associated key from the database
                                    database.deleteAndWriteToDb(userArgs.key);

                                    writeLock.unlock();

                                    // Send response from server to client indicating that 'delete' operation was successful
                                    sendResponse(Response.deleteOk(), output);

                                } else {
                                    // If the given key was not found, send an ERROR response
                                    sendResponse(Response.deleteError("No such key"), output);
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

                } catch (InterruptedException e) {

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

}
