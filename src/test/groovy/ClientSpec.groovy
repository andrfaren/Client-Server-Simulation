import client.Client
import server.Main
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("Unit test of the client class")
class ClientSpec extends Specification {

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

    def "Send a message to the server, receive a response and print it"() {

        given: "a client"
        @Subject
        def client = new Client()

        when:
        client.sendMessage(new String[]{"-t", "get", "-k", "1"})

        then: "the server responds with a message"
        client.messageReceived == "{\"response\":\"ERROR\",\"reason\":\"No such key\"}"

    }
}
