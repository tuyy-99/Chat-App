// Server.java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server {
    private final int port;
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server on port " + port + " ...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                // create handler (it will perform the username handshake)
                Thread t = new Thread(new ClientHandler(this, socket));
                // keep server running even if handlers finish; don't make daemon so JVM won't exit prematurely
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Register - returns true if username accepted
    public boolean registerClient(String username, ClientHandler handler) {
        if (username == null) return false;
        username = username.trim();
        if (username.isEmpty()) return false;
        return (clients.putIfAbsent(username, handler) == null);
    }

    public void unregisterClient(String username) {
        if (username != null && clients.remove(username) != null) {
            broadcastSystemMessage(username + " has left the chat.");
        }
    }

    // Broadcast to everyone
    public void broadcast(String fromUser, String message) {
        String msg = fromUser + ": " + message;
        for (ClientHandler ch : clients.values()) {
            ch.send(msg);
        }
    }

    // Broadcast a system message
    public void broadcastSystemMessage(String message) {
        String msg = "[SYSTEM] " + message;
        for (ClientHandler ch : clients.values()) {
            ch.send(msg);
        }
    }

    // Send a private message. return true if recipient found
    public boolean privateMessage(String fromUser, String toUser, String message) {
        ClientHandler recipient = clients.get(toUser);
        if (recipient != null) {
            recipient.send("[PM from " + fromUser + "] " + message);
            ClientHandler sender = clients.get(fromUser);
            if (sender != null) sender.send("[PM to " + toUser + "] " + message);
            return true;
        } else {
            ClientHandler sender = clients.get(fromUser);
            if (sender != null) sender.send("[SYSTEM] User '" + toUser + "' not found.");
            return false;
        }
    }

    // Return a snapshot list of usernames
    public List<String> listUsers() {
        return new ArrayList<>(clients.keySet());
    }

    public static void main(String[] args) {
        int port = 9000;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        new Server(port).start();
    }
}
