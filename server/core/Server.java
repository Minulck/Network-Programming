package server.core;

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

// Server component imports
import server.auction.AuctionManager;
import server.chat.ChatManager;
import server.ftp.FTPServer;
import server.notifications.UDPNotificationService;
import server.security.SecureConnectionManager;
import server.auction.Auction;

// HTTP server
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Server {
    private static final int PORT = 5000;
    private static final int WS_PORT = 5001;
    private static final int HTTP_PORT = 8080;
    private static List<MessageSender> senders = new CopyOnWriteArrayList<>();
    private static List<WebSocketHandler> wsHandlers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("===========================================");
        System.out.println("   BidEasy - Enterprise Auction Platform");
        System.out.println("===========================================");
        
        // Start UDP Notification Service (Feature 2)
        UDPNotificationService.start();
        
        // Start Chat Manager with NIO (Feature 1)
        ChatManager.start();
        
        // Start FTP Server for image uploads
        FTPServer.start();
        
        // Start Secure Connection Manager (Feature 4)
        SecureConnectionManager.start(senders);
        
        // Start socket server for console clients
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Main Server started on port " + PORT);

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
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down BidEasy Server...");
            UDPNotificationService.shutdown();
            FTPServer.shutdown();
            System.out.println("Server shutdown complete.");
        }));

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
                        // Broadcast updated user list
                        broadcastUserList();
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
        // Add FTP upload endpoint
        httpServer.createContext("/ftp-upload", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, 0);
                    exchange.getResponseBody().close();
                    return;
                }
                
                try {
                    // Enable CORS
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
                    
                    // Parse multipart form data
                    String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                    if (contentType != null && contentType.contains("multipart/form-data")) {
                        String boundary = contentType.split("boundary=")[1];
                        InputStream is = exchange.getRequestBody();
                        
                        // Read the entire input
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        byte[] allData = baos.toByteArray();
                        
                        // Parse filename and file data from multipart
                        String boundaryMarker = "--" + boundary;
                        String data = new String(allData, "ISO-8859-1");
                        
                        // Find filename
                        int filenameStart = data.indexOf("filename=\"") + 10;
                        int filenameEnd = data.indexOf("\"", filenameStart);
                        String originalFilename = data.substring(filenameStart, filenameEnd);
                        
                        // Find file data start (after double CRLF)
                        int headerEnd = data.indexOf("\r\n\r\n", filenameStart) + 4;
                        int dataEnd = data.indexOf(boundaryMarker, headerEnd) - 2; // -2 for CRLF before boundary
                        
                        // Extract file bytes
                        byte[] fileData = new byte[dataEnd - headerEnd];
                        System.arraycopy(allData, headerEnd, fileData, 0, fileData.length);
                        
                        // Connect to FTP server and upload
                        String uploadedFilename = uploadToFTPServer(originalFilename, fileData);
                        
                        String response = "SUCCESS|" + uploadedFilename;
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                        String error = "ERROR|Invalid content type";
                        exchange.sendResponseHeaders(400, error.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(error.getBytes());
                        os.close();
                    }
                } catch (Exception e) {
                    String error = "ERROR|" + e.getMessage();
                    exchange.sendResponseHeaders(500, error.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(error.getBytes());
                    os.close();
                }
            }
        });
        
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) path = "/dashboard.html";
                
                // Serve uploaded images
                if (path.startsWith("/uploads/")) {
                    File file = new File(path.substring(1)); // Remove leading /
                    if (file.exists()) {
                        String contentType = "image/jpeg";
                        if (path.endsWith(".png")) contentType = "image/png";
                        else if (path.endsWith(".gif")) contentType = "image/gif";
                        else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
                        
                        exchange.getResponseHeaders().set("Content-Type", contentType);
                        exchange.sendResponseHeaders(200, file.length());
                        
                        try (FileInputStream fis = new FileInputStream(file);
                             OutputStream os = exchange.getResponseBody()) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }
                        return;
                    }
                }
                
                File file = new File("client" + path);
                if (file.exists()) {
                    // Set proper content type with UTF-8 encoding
                    String contentType = "text/html; charset=UTF-8";
                    if (path.endsWith(".css")) {
                        contentType = "text/css; charset=UTF-8";
                    } else if (path.endsWith(".js")) {
                        contentType = "application/javascript; charset=UTF-8";
                    }
                    exchange.getResponseHeaders().set("Content-Type", contentType);
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
        
        System.out.println("\n===========================================");
        System.out.println("All services started successfully!");
        System.out.println("- Main Server: " + PORT);
        System.out.println("- WebSocket: " + WS_PORT);
        System.out.println("- HTTP: " + HTTP_PORT);
        System.out.println("- FTP: 5010");
        System.out.println("- Chat (NIO): 5002");
        System.out.println("- UDP Notifications: 5003");
        System.out.println("- Secure SSL/TLS: 5005");
        System.out.println("===========================================\n");
    }

    public static void broadcast(String message) {
        for (MessageSender sender : senders) {
            sender.sendMessage(message);
        }
    }
    
    private static String uploadToFTPServer(String originalFilename, byte[] fileData) throws IOException {
        String uploadedFilename = "";
        try (Socket ftpSocket = new Socket("localhost", 5010);
             DataOutputStream out = new DataOutputStream(ftpSocket.getOutputStream());
             DataInputStream in = new DataInputStream(ftpSocket.getInputStream())) {
            
            // Send UPLOAD command
            String command = "UPLOAD|" + originalFilename + "|" + fileData.length;
            out.writeUTF(command);
            out.flush();
            
            // Wait for OK response
            String response = in.readUTF();
            if (response.startsWith("OK|")) {
                uploadedFilename = response.split("\\|")[1];
                
                // Send file data
                out.write(fileData);
                out.flush();
                
                // Wait for SUCCESS response
                response = in.readUTF();
                if (!response.startsWith("SUCCESS|")) {
                    throw new IOException("FTP upload failed: " + response);
                }
            } else {
                throw new IOException("FTP server error: " + response);
            }
        }
        return uploadedFilename;
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
                    
                    System.out.println("WebSocket user " + username + " logged in. Sending existing auctions...");
                    
                    // Send all existing ongoing auctions to the newly connected client
                    List<Auction> existingAuctions = AuctionManager.getAllAuctions();
                    System.out.println("Found " + existingAuctions.size() + " existing auctions to send");
                    
                    for (Auction auction : existingAuctions) {
                        String auctionMsg = Protocol.newAuctionMessage(
                            auction.getId(), 
                            auction.getName(), 
                            auction.getStartPrice(), 
                            auction.getDurationSec(),
                            auction.getImageName()
                        );
                        System.out.println("Sending auction to WebSocket user " + username + ": " + auctionMsg);
                        sendMessage(auctionMsg);
                        
                        // If there's already a bid on this auction, send an update
                        if (auction.getHighestBidder() != null) {
                            String updateMsg = Protocol.updateMessage(
                                auction.getId(), 
                                auction.getCurrentBid(), 
                                auction.getHighestBidder()
                            );
                            System.out.println("Sending update to WebSocket user " + username + ": " + updateMsg);
                            sendMessage(updateMsg);
                        }
                    }
                    
                    // Send initial user list
                    broadcastUserList();
                } else {
                    sendMessage(Protocol.errorMessage("Please login first"));
                }
            } else {
                // Handle chat messages
                if (message.startsWith("CHAT_")) {
                    handleChatMessage(message);
                } else {
                    // Handle auction messages
                    AuctionManager.processMessage(message, this);
                }
            }
        }
        
        private void handleChatMessage(String message) {
            String[] parts = message.split("\\|", 2);
            String command = parts[0];
            
            if (command.equals("CHAT_GET_USERS")) {
                sendUserList();
            } else if (command.equals("CHAT_PRIVATE") && parts.length == 2) {
                String[] privateParts = parts[1].split("\\|", 2);
                if (privateParts.length == 2) {
                    String targetUser = privateParts[0];
                    String privateMsg = privateParts[1];
                    sendPrivateMessage(targetUser, privateMsg);
                }
            }
        }
        
        private void sendPrivateMessage(String targetUser, String message) {
            // Find target user
            WebSocketHandler targetHandler = null;
            for (WebSocketHandler handler : wsHandlers) {
                if (targetUser.equals(handler.getUsername())) {
                    targetHandler = handler;
                    break;
                }
            }
            
            if (targetHandler != null) {
                // Send to recipient
                targetHandler.sendMessage("CHAT_PRIVATE|" + username + "|" + message);
                // Echo back to sender
                sendMessage("CHAT_PRIVATE_SENT|" + targetUser + "|" + message);
            } else {
                sendMessage("CHAT_ERROR|User " + targetUser + " not found or offline");
            }
        }
        
        private void sendUserList() {
            StringBuilder userList = new StringBuilder("CHAT_USERS|");
            for (WebSocketHandler handler : wsHandlers) {
                if (handler.getUsername() != null && !handler.getUsername().equals(username)) {
                    userList.append(handler.getUsername()).append(",");
                }
            }
            sendMessage(userList.toString());
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
    
    // Broadcast user list to all connected clients
    public static void broadcastUserList() {
        for (WebSocketHandler handler : wsHandlers) {
            if (handler.getUsername() != null) {
                handler.sendUserList();
            }
        }
    }
}