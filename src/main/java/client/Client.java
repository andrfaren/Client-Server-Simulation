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
import config.Config;

public class Client {
    public String messageForServer;
    public String messageReceived;
    public Client() {
        System.out.println("Client started!");
    }

    private void setMessageFromFile(String inputFileName) {

        //            Path inputFilePath = Path.of("C:\\Users\\andre\\IdeaProjects\\JSON Database\\task\\src\\client\\data\\" + userArgs.inputFile);
        Path inputFilePath = Path.of("src/client/data/" + inputFileName);

        try {
            this.messageForServer = Files.readString(inputFilePath); // UTF 8

        } catch (IOException e) {

        }
    }

    public void sendMessage(String[] args) throws IOException {

        try (
                Socket socket = new Socket(InetAddress.getByName(Config.IP_ADDRESS), Config.PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        )

        {
            // Parse args and load them into an object
            Args userArgs = new Args();

            JCommander.newBuilder()
                    .addObject(userArgs)
                    .build()
                    .parse(args);

            if (userArgs.inputFile != null) {

                setMessageFromFile(userArgs.inputFile);

            } else {

                // Create the JSON object containing the user's command
                Gson gson = new GsonBuilder().create();
                this.messageForServer = gson.toJson(userArgs);
            }

            // Send command to the server
            output.writeUTF(messageForServer);

            // Print what was sent to the server
            System.out.println("Sent: " + messageForServer);

            // Read the response from the server
            this.messageReceived = input.readUTF();
            System.out.println("Received: " + messageReceived);

        } catch (IOException e) {

        }
    }
}
