package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import shared.MessageSender;
import shared.Protocol;

// WebSocket imports (assuming java-websocket library)
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;

// HTTP server
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class Server {
    private static final int PORT = 5000;
    private static final int WS_PORT = 5001;
    private static final int HTTP_PORT = 8080;
    private static List<MessageSender> senders = new CopyOnWriteArrayList<>();
    private static List<WebSocketHandler> wsHandlers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        // Start socket server for console clients
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("BidEasy Server started on port " + PORT);

        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New console client connected: " + clientSocket);

                    ClientHandler handler = new ClientHandler(clientSocket, senders);
                    senders.add(handler);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Start WebSocket server
        WebSocketServer wsServer = new WebSocketServer(new InetSocketAddress(WS_PORT)) {
            @Override
            public void onStart() {
                System.out.println("WebSocket server started successfully");
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("New WebSocket client connected: " + conn.getRemoteSocketAddress());
                WebSocketHandler handler = new WebSocketHandler(conn);
                wsHandlers.add(handler);
                senders.add(handler);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                for (WebSocketHandler h : wsHandlers) {
                    if (h.conn == conn) {
                        h.onMessage(message);
                        break;
                    }
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("WebSocket client disconnected: " + conn.getRemoteSocketAddress());
                for (WebSocketHandler h : wsHandlers) {
                    if (h.conn == conn) {
                        wsHandlers.remove(h);
                        senders.remove(h);
                        break;
                    }
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }
        };
        wsServer.start();
        System.out.println("WebSocket server started on port " + WS_PORT);

        // Start HTTP server for serving HTML
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) path = "/dashboard.html";
                File file = new File("client" + path);
                if (file.exists()) {
                    exchange.sendResponseHeaders(200, file.length());
                    try (FileInputStream fis = new FileInputStream(file);
                         OutputStream os = exchange.getResponseBody()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                }
            }
        });
        httpServer.start();
        System.out.println("HTTP server started on port " + HTTP_PORT);
    }

    public static void broadcast(String message) {
        for (MessageSender sender : senders) {
            sender.sendMessage(message);
        }
    }

    static class WebSocketHandler implements MessageSender {
        private WebSocket conn;
        private String username;

        WebSocketHandler(WebSocket conn) {
            this.conn = conn;
        }

        public void onMessage(String message) {
            if (username == null) {
                if (message.startsWith(Protocol.LOGIN + "|")) {
                    username = message.split("\\|")[1];
                    sendMessage(Protocol.welcomeMessage(username));
                } else {
                    sendMessage(Protocol.errorMessage("Please login first"));
                }
            } else {
                AuctionManager.processMessage(message, this);
            }
        }

        @Override
        public void sendMessage(String msg) {
            if (conn.isOpen()) {
                conn.send(msg);
            }
        }

        @Override
        public String getUsername() {
            return username;
        }
    }
}