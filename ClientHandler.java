// ClientHandler.java
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private String username;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    // Thread-safe send
    public synchronized void send(String message) {
        if (out != null) out.println(message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Ask for username
            out.println("[SYSTEM] Enter username:");
            String name = in.readLine();
            if (name == null) { closeEverything(); return; }
            name = name.trim();

            // Try to register; if fail ask again up to a few times
            int attempts = 0;
            while (!server.registerClient(name, this)) {
                attempts++;
                if (attempts >= 3) {
                    out.println("[SYSTEM] Username already taken. Disconnecting.");
                    closeEverything();
                    return;
                }
                out.println("[SYSTEM] Username already taken. Enter a different username:");
                name = in.readLine();
                if (name == null) { closeEverything(); return; }
                name = name.trim();
            }

            this.username = name;
            out.println("[SYSTEM] Welcome, " + username + "!");
            server.broadcastSystemMessage(username + " has joined the chat.");

            // Main loop
            String line;
            while (running && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Parse command
                if (line.equalsIgnoreCase("QUIT")) {
                    out.println("[SYSTEM] Goodbye!");
                    break;
                } else if (line.equalsIgnoreCase("LIST")) {
                    out.println("[SYSTEM] Connected users: " + String.join(", ", server.listUsers()));
                } else if (line.toUpperCase().startsWith("PM ")) {
                    // PM format: PM <user> <text>
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 3) {
                        out.println("[SYSTEM] Invalid PM format. Use: PM <user> <message>");
                    } else {
                        String toUser = parts[1];
                        String msg = parts[2];
                        server.privateMessage(username, toUser, msg);
                    }
                } else if (line.toUpperCase().startsWith("MSG ")) {
                    String msg = line.substring(4);
                    server.broadcast(username, msg);
                } else {
                    // default: treat as broadcast
                    server.broadcast(username, line);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error for user " + username + ": " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void closeEverything() {
        running = false;
        try {
            if (username != null) {
                server.unregisterClient(username);
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
