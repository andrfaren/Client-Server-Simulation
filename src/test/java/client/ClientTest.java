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

        serverThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client client = new Client();
        client.sendMessage(new String[]{"-t", "get", "-k", "1"});

        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", client.messageReceived);
    }

}