# Client-Server-Simulation

A Java project for simulating concurrent interaction between a client and a database server.

The server accepts commands in JSON using the command-line. The user can retrieve, remove, and add new records to the database.
Data is stored in a .json file and is represented by a HashMap.

The client can send the server a String in JSON that is taken from the main method's command line arguments.

The client can send commands to get, set, delete records in the database, and terminate the server.

For example, the client can send a command like this: {"-t", "get", "-k", "1"}
Where "t" is the type of the command (in this case, it is "get") and "k" is the key whose value the client would like to retrieve.

If the database.json file has an entry of the form: {"1" : "Hello World"}, then the server will reply with a JSON String response: "{"Response": "OK", "value" : "Hello World"}". 

Currently supports multiple concurrent requests - one thread per request.

Libraries used:
 - GSON
 - JCommander
