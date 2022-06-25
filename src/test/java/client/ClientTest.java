package client;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void sendMessage() throws IOException {

        Thread serverThread = new Thread() {
            public void run() {
                server.Main.main(new String[]{""});
            }
        };

        Thread clientThread = new Thread() {
            public void run() {
                Client client = new Client();
                try {
                    client.sendMessage(new String[]{"-t", "get", "-k", "1"});
                    String messageReceived = client.messageReceived;
                    assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", client.messageReceived);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        serverThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clientThread.start();
    }

}