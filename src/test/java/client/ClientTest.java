package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Main;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    private static Client client;
    @BeforeAll
    static void setup() {
        server.Main.main(new String[]{""});
    }

    @BeforeEach
    void init() {
        client = new Client();
    }

    @Test
    void sendMessage() throws IOException {

        client.sendMessage(new String[]{"-t", "get", "-k", "1"});

        String messageReceived = client.messageReceived;
        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", client.messageReceived);
    }

    @AfterAll
    static void tearDownAll() {
        server.Main.stopServer();
    }
}