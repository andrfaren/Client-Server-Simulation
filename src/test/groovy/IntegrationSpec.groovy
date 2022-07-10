import client.Client
import server.Main
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Narrative(""" Each test method uses a client object to send one request (get, set, delete, or exit) to a running
server. The get, set, and delete requests retrieve or modify key-value pairs residing in a database file on the server.
Communication between the client and server happens through JSON strings. After each request is sent, the client always 
receives a message indicating whether the request was successful or not.
""")
@Title("Integration test of the application")
class IntegrationSpec extends Specification {

    def startServer() {
        Thread serverThread = new Thread(() -> Main.main(new String[]{""}));

        serverThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    def setupSpec() {
        startServer()
    }

    def "Receive the ERROR response when attempting to get a value that does not exist"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a get request for a non-existent value"
        client.sendMessage(new String[]{"-t", "get", "-k", "1"})

        then: "the server responds with the error message 'No such key'"
        client.messageReceived == "{\"response\":\"ERROR\",\"reason\":\"No such key\"}"
    }

    def "Receive the OK response when setting a new value"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a set request with a key and value"
        client.sendMessage(new String[]{"-t", "set", "-k", "1", "-v", "HelloWorld!"})

        then: "the server responds with the message 'OK'"
        client.messageReceived == "{\"response\":\"OK\"}"
    }

    def "Receive the OK response along with the retrieved value when getting an existing value by its key"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a get request with a key for an existing value"
        client.sendMessage(new String[]{"-t", "get", "-k", "1"})

        then: "the server responds with the message 'OK'"
        client.messageReceived == "{\"response\":\"OK\",\"value\":\"HelloWorld!\"}"
    }

    def "Receive the OK response when deleting an existing value by its key"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a delete request with a key for an existing value"
        client.sendMessage(new String[]{"-t", "delete", "-k", "1"})

        then: "the server responds with the message 'OK'"
        client.messageReceived == "{\"response\":\"OK\"}"
    }

    def "Receive the ERROR response when deleting a non-existent value by its key"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a delete request with a key for a non-existent value"
        client.sendMessage(new String[]{"-t", "delete", "-k", "1"})

        then: "the server responds with the error message 'No such key'"
        client.messageReceived == "{\"response\":\"ERROR\",\"reason\":\"No such key\"}"
    }

    def "Receive the ERROR response when getting a value that had been deleted"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends a get request with a key for a non-existent value"
        client.sendMessage(new String[]{"-t", "get", "-k", "1"})

        then: "the server responds with the error message 'No such key'"
        client.messageReceived == "{\"response\":\"ERROR\",\"reason\":\"No such key\"}"
    }

    def "Receive the OK response when sending an exit request"() {
        given: "a client"
        @Subject
        def client = new Client()

        when: "the client sends an exit request"
        client.sendMessage(new String[]{"-t", "exit"})

        then: "the server responds with the message 'OK' and stops running"
        client.messageReceived == "{\"response\":\"OK\"}"
    }


}
