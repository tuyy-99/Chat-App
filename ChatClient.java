// ChatClient.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient extends Application {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ListView<String> messagesList;
    private TextField inputField;
    private Button sendBtn;

    private String host = "localhost";
    private int port = 9000;
    private String username = "guest";

    @Override
    public void start(Stage stage) {
        messagesList = new ListView<>();
        messagesList.setFocusTraversable(false);

        inputField = new TextField();
        inputField.setPromptText("Type a message...");

        sendBtn = new Button("Send");
        sendBtn.setDefaultButton(true);

        HBox inputArea = new HBox(8, inputField, sendBtn);
        inputArea.setPadding(new Insets(10));
        HBox.setHgrow(inputField, Priority.ALWAYS);

        VBox root = new VBox(6, messagesList, inputArea);
        root.setPadding(new Insets(8));
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 640, 480);

        // Try to load style.css from classpath; if not found, load from file system (same folder)
        try {
            if (getClass().getResource("style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            } else {
                // fallback to file path (when running from IDE / file system)
                scene.getStylesheets().add(Paths.get("style.css").toUri().toString());
            }
        } catch (Exception ignored) {}

        stage.setScene(scene);
        stage.setTitle("ChatClient - GUI");
        stage.show();

        // Handlers
        inputField.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ENTER) sendMessage();
        });
        sendBtn.setOnAction(e -> sendMessage());

        // Ask host/port/username
        TextInputDialog hostDlg = new TextInputDialog(host);
        hostDlg.setTitle("Connect");
        hostDlg.setHeaderText("Server host");
        hostDlg.setContentText("Host:");
        host = hostDlg.showAndWait().orElse(host);

        TextInputDialog portDlg = new TextInputDialog(String.valueOf(port));
        portDlg.setTitle("Connect");
        portDlg.setHeaderText("Server port");
        portDlg.setContentText("Port:");
        try {
            port = Integer.parseInt(portDlg.showAndWait().orElse(String.valueOf(port)));
        } catch (NumberFormatException ignored) {}

        TextInputDialog nameDlg = new TextInputDialog(username);
        nameDlg.setTitle("Connect");
        nameDlg.setHeaderText("Choose username");
        nameDlg.setContentText("Username:");
        username = nameDlg.showAndWait().orElse(username);

        connect(); // background

        stage.setOnCloseRequest(e -> {
            closeEverything();
            // allow executor to shutdown briefly
            executor.shutdownNow();
            Platform.exit();
        });
    }

    private void connect() {
        executor.submit(() -> {
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                // The server asks for username; sending it immediately is fine
                out.println(username);

                Platform.runLater(() -> messagesList.getItems().add("[SYSTEM] Connected to " + host + ":" + port));

                String line;
                while ((line = in.readLine()) != null) {
                    final String received = line;
                    Platform.runLater(() -> messagesList.getItems().add(received));
                }
            } catch (IOException ex) {
                Platform.runLater(() -> messagesList.getItems().add("[SYSTEM] Connection error: " + ex.getMessage()));
            } finally {
                closeEverything();
            }
        });
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || out == null) return;

        // Send as-is (server treats plain text as broadcast)
        out.println(text);

        // show local copy
        messagesList.getItems().add("Me: " + text);
        inputField.clear();
    }

    private void closeEverything() {
        try {
            if (out != null) out.println("QUIT");
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        if (!executor.isShutdown()) executor.shutdownNow();

        // show disconnected message on UI thread (only if Platform is available)
        try {
            Platform.runLater(() -> messagesList.getItems().add("[SYSTEM] Disconnected."));
        } catch (IllegalStateException ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
