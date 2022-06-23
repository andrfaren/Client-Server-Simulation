package client;

import org.junit.jupiter.api.Test;
import server.Main;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void sendMessage() throws IOException {

        server.Main.main(new String[] {""});
        server.Main serverMain = new Main();

        client.Client client = new Client();
        client.sendMessage(new String[] {"-t", "get", "-k", "1"});

        String messageReceived = client.messageReceived;
        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", client.messageReceived);
        server.Main.stopServer();


    }
}