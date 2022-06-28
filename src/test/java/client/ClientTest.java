package client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    @BeforeAll
    static void setUp() {
        Thread serverThread = new Thread(() -> Main.main(new String[]{""}));

        serverThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void sendMessage() {

        // Check response when getting a value that does not exist
        Client clientGetNotExists = new Client();
        clientGetNotExists.sendMessage(new String[]{"-t", "get", "-k", "1"});
        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", clientGetNotExists.messageReceived);

        // Check response when setting a value that does not exist
        Client clientSet = new Client();
        clientSet.sendMessage(new String[]{"-t", "set", "-k", "1", "-v", "HelloWorld!"});
        assertEquals("{\"response\":\"OK\"}", clientSet.messageReceived);

        // Check response when getting a value that exists
        Client clientGetExists = new Client();
        clientGetExists.sendMessage(new String[]{"-t", "get", "-k", "1"});
        assertEquals("{\"response\":\"OK\",\"value\":\"HelloWorld!\"}", clientGetExists.messageReceived);

        // Check response when deleting a value that exists
        Client clientDeleteExists = new Client();
        clientDeleteExists.sendMessage(new String[]{"-t", "delete", "-k", "1"});
        assertEquals("{\"response\":\"OK\"}", clientDeleteExists.messageReceived);

        // Check response when deleting a value that does not exist
//        Client clientDeleteNotExists = new Client();
//        clientDeleteNotExists.sendMessage(new String[]{"-t", "delete", "-k", "1"});
//        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", clientDeleteNotExists.messageReceived);

        // Check response when getting a deleted value (i.e. getting value that no longer exists)
        Client clientGetDeleted = new Client();
        clientGetDeleted.sendMessage(new String[]{"-t", "get", "-k", "1"});
        assertEquals("{\"response\":\"ERROR\",\"reason\":\"No such key\"}", clientGetDeleted.messageReceived);

        // Check response for exit function
        Client clientExit = new Client();
        clientExit.sendMessage(new String[]{"-t", "exit"});
        assertEquals("{\"response\":\"OK\"}", clientExit.messageReceived);

    }

}