package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.Args;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {

        // Server's IP address
        String ipAddress = "127.0.0.1";

        // Port to be used for communication with the server
        int port = 23456;

        // Create a new socket using the server's IP address and port
        Socket socket = new Socket(InetAddress.getByName(ipAddress), port);
        System.out.println("Client started!");

        // Create input and output streams
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        Args userArgs = new Args();

        JCommander.newBuilder()
                .addObject(userArgs)
                .build()
                .parse(args);

        Gson gson = new GsonBuilder()
                .create();

        String messageForServer;

        if (userArgs.inputFile != null) {

//            Path inputFilePath = Path.of("C:\\Users\\andre\\IdeaProjects\\JSON Database\\task\\src\\client\\data\\" + userArgs.inputFile);
            Path inputFilePath = Path.of("src/client/data/" + userArgs.inputFile);

            messageForServer = Files.readString(inputFilePath); // UTF 8

        } else {

            // Create the JSON object containing the user's command
            messageForServer = gson.toJson(userArgs);
        }

        // Send command to the server
        output.writeUTF(messageForServer);

        // Print what was sent to the server
        System.out.println("Sent: " + messageForServer);

        // Read the response from the server
        System.out.println("Received: " + input.readUTF());

        input.close();
        output.close();
        socket.close();
    }
}
