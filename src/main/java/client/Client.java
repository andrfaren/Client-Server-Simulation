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
    private Args userArgs;

    public Client() {
        System.out.println("Client started!");
    }

    public void sendMessage(String[] args) {

        try (
                Socket socket = new Socket(InetAddress.getByName(Config.IP_ADDRESS), Config.PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        )

        {
            try {
                this.messageForServer = getJSONStringFromArgs(args);
            } catch (IOException e) {
                System.out.println("Problem reading file containing command. Check that it exists and is formatted properly.");
                System.exit(1);
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

    public String setMessageFromFile(String inputFileName) throws IOException {
        // Path inputFilePath = Path.of("C:\\Users\\andre\\IdeaProjects\\JSON Database\\task\\src\\client\\data\\" + userArgs.inputFile);
        Path inputFilePath = Path.of("src/client/data/" + inputFileName);
        return Files.readString(inputFilePath); // UTF 8
    }

    public String getJSONStringFromArgs(String[] args) throws IOException {

        // Parse args and load them into an object
        this.userArgs = new Args();

        JCommander.newBuilder()
                .addObject(userArgs)
                .build()
                .parse(args);

        if (this.userArgs.inputFile != null) {
                return setMessageFromFile(this.userArgs.inputFile);
        } else {

            // Create the JSON object containing the user's command
            Gson gson = new GsonBuilder().create();

            // Assign the JSON String
            return gson.toJson(this.userArgs);
        }
    }
}
