package server.response;

public class Response {
    ResponseType response; // OK or ERROR
    String reason;
    String value;

    public Response(ResponseType response) {
        this.response = response;
    }
    public Response(ResponseType response, String value, String reason) {
        this.response = response;
        this.value = value;
        this.reason = reason;
    }

}
