# Chat App using JAVAFX

A modern chat application built with **Java**, **JavaFX**, and **socket networking**. It includes a graphical client with a clean UI and a multithreaded server that supports multiple users, private messaging, and system broadcasts.

This version is based entirely on the JavaFX client (`ChatClient.java`) and the multi‑client server (`Server.java`, `ClientHandler.java`).

---

## 🌟 Features

* Polished JavaFX chat interface
* Smooth message list updates using JavaFX threads
* Username prompts for host, port, and name
* Private messaging (`PM <user> <message>`)
* Broadcast messaging (plain text)
* Shows connected/disconnected system messages
* UTF‑8 safe
* Fully multithreaded server
* Each user handled in a separate thread

---

## 📁 Project Structure

```
chat-app/
│
├── ChatClient.java        # JavaFX Client UI
├── Server.java            # Multi-threaded server
├── ClientHandler.java     # Per‑client session handler
├── style.css              # JavaFX styling
└── README.md
```

---

## 🧩 Prerequisites

### 1. Install Java 17+ (recommended)

Check:

```bash
java -version
```

### 2. Install JavaFX SDK

Download from: [https://openjfx.io](https://openjfx.io)

You must extract it and note the path to:

```
.../javafx-sdk-25.0.1/lib
```

---

## 🛠️ Running the Server

Compile the server files:

```bash
javac Server.java ClientHandler.java
```

Run the server (default port 9000):

```bash
java Server 9000
```

You should see:

```
Starting server on port 9000 ...
Server started. Waiting for clients...
```

---

## 🖥️ Running the JavaFX Client

Compile with JavaFX modules:

```bash
javac --module-path "PATH_TO_JAVAFX/lib" --add-modules javafx.controls,javafx.fxml ChatClient.java
```

Run the client:

```bash
java --module-path "PATH_TO_JAVAFX/lib" --add-modules javafx.controls,javafx.fxml ChatClient
```

On launch, the client will ask for:

* server host
* server port
* username

Then it connects automatically.

Open multiple client windows to simulate multiple users.

---
## 📸 Screenshots

Here’s a quick look at the Java Chat App in action.

### 🟦 Login Screen
![alt text](image.png)
![alt text](image-1.png)

### 🟩 Chat Window
![alt text](image-2.png)
---


## 🎨 UI Styling (style.css)

You can fully customize the chat look using the included CSS file.
Example entries:

```css
.root {
    -fx-background-color: #1e1e1e;
}
.list-view {
    -fx-control-inner-background: #2b2b2b;
    -fx-font-size: 14px;
    -fx-text-fill: white;
}
```

---

## 💬 Chat Commands

| Command           | Description               |
| ----------------- | ------------------------- |
| `text`            | Sends broadcast message   |
| `PM user message` | Sends a private message   |
| `LIST`            | Shows connected usernames |
| `QUIT`            | Disconnects from server   |

---

## 🧪 Example Session

User A sees:

```
[SYSTEM] Connected to localhost:9000
[SYSTEM] mira has joined the chat.
```

User B sends:

```
PM alex hello!
```

User A receives:

```
[PM from mira] hello!
```

---
