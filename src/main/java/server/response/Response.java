package server.response;

public class Response {
    ResponseType response; // OK or ERROR
    String reason;
    String value;

    private Response(ResponseType response) {
        this.response = response;
    }
    private Response(ResponseType response, String value) {
        this.response = response;
        this.value = value;
    }
    private Response(ResponseType response, String value, String reason) {
        this.response = response;
        this.value = value;
        this.reason = reason;
    }

    public static Response setOk() {
        return new Response(ResponseType.OK);
    }

    public static Response getOk(String retrievedValue) {
        return new Response(ResponseType.OK, retrievedValue);
    }

    public static Response getError(String reason) {
        return new Response(ResponseType.ERROR, null, reason);
    }

    public static Response deleteOk() {
        return new Response(ResponseType.OK);
    }

    public static Response deleteError(String reason) {
        return new Response(ResponseType.ERROR, null, reason);
    }

    public static Response exitOk() {
        return new Response(ResponseType.OK);
    }
}
