package server.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 1: Chat System using Java NIO (Non-blocking I/O)
 * Demonstrates: SocketChannel, Selector, SelectionKey
 * Contributor: Member 1
 */
public class ChatManager implements Runnable {
    private static final int CHAT_PORT = 5002;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private Map<SocketChannel, String> clients = new ConcurrentHashMap<>();
    private Map<String, SocketChannel> userChannels = new ConcurrentHashMap<>();

    public ChatManager() throws IOException {
        // Create selector for non-blocking I/O
        selector = Selector.open();
        
        // Setup server channel
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(CHAT_PORT));
        serverChannel.configureBlocking(false);
        
        // Register accept operation
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("Chat Server (NIO) started on port " + CHAT_PORT);
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Wait for events (non-blocking)
                selector.select();
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                    
                    iter.remove();
                }
            }
        } catch (IOException e) {
            System.err.println("Chat Manager error: " + e.getMessage());
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        
        System.out.println("New chat client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        try {
            int bytesRead = clientChannel.read(buffer);
            
            if (bytesRead == -1) {
                // Client disconnected
                String username = clients.get(clientChannel);
                if (username != null) {
                    broadcastMessage("SYSTEM", username + " left the chat");
                    clients.remove(clientChannel);
                    userChannels.remove(username);
                    broadcastUserList();
                }
                clientChannel.close();
                return;
            }
            
            if (bytesRead > 0) {
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                processMessage(clientChannel, message);
            }
        } catch (IOException e) {
            String username = clients.get(clientChannel);
            if (username != null) {
                try {
                    broadcastMessage("SYSTEM", username + " disconnected");
                    broadcastUserList();
                } catch (IOException ex) {
                    System.err.println("Error broadcasting disconnect: " + ex.getMessage());
                }
                clients.remove(clientChannel);
                userChannels.remove(username);
            }
            try {
                clientChannel.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }

    private void processMessage(SocketChannel clientChannel, String message) throws IOException {
        String[] parts = message.split("\\|", 2);
        String command = parts[0];
        
        if (command.equals("CHAT_LOGIN") && parts.length == 2) {
            String username = parts[1];
            clients.put(clientChannel, username);
            userChannels.put(username, clientChannel);
            sendToClient(clientChannel, "CHAT_WELCOME|Welcome to auction chat, " + username + "!");
            
            // Send updated user list to all clients
            broadcastUserList();
            
            broadcastMessage("SYSTEM", username + " joined the chat");
        } else if (command.equals("CHAT_MSG") && parts.length == 2) {
            String username = clients.get(clientChannel);
            if (username != null) {
                broadcastMessage(username, parts[1]);
            }
        } else if (command.equals("CHAT_PRIVATE") && parts.length == 2) {
            String[] privateParts = parts[1].split("\\|", 2);
            if (privateParts.length == 2) {
                String targetUser = privateParts[0];
                String privateMsg = privateParts[1];
                String sender = clients.get(clientChannel);
                sendPrivateMessage(sender, targetUser, privateMsg);
            }
        } else if (command.equals("CHAT_GET_USERS")) {
            sendUserList(clientChannel);
        }
    }

    private void broadcastMessage(String sender, String message) throws IOException {
        String formattedMsg = "CHAT_BROADCAST|" + sender + "|" + message;
        ByteBuffer buffer = ByteBuffer.wrap(formattedMsg.getBytes(StandardCharsets.UTF_8));
        
        for (SocketChannel client : clients.keySet()) {
            buffer.rewind();
            client.write(buffer);
        }
        System.out.println("Chat [" + sender + "]: " + message);
    }

    private void sendPrivateMessage(String sender, String recipient, String message) throws IOException {
        SocketChannel recipientChannel = userChannels.get(recipient);
        SocketChannel senderChannel = userChannels.get(sender);
        
        if (recipientChannel != null && recipientChannel.isConnected()) {
            // Send to recipient
            String formattedMsg = "CHAT_PRIVATE|" + sender + "|" + message;
            sendToClient(recipientChannel, formattedMsg);
            
            // Echo back to sender so they see their sent message
            if (senderChannel != null && senderChannel.isConnected()) {
                String echoMsg = "CHAT_PRIVATE_SENT|" + recipient + "|" + message;
                sendToClient(senderChannel, echoMsg);
            }
        } else {
            // User not found or offline
            if (senderChannel != null && senderChannel.isConnected()) {
                sendToClient(senderChannel, "CHAT_ERROR|User " + recipient + " not found or offline");
            }
        }
    }

    private void sendToClient(SocketChannel client, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        client.write(buffer);
    }
    
    private void sendUserList(SocketChannel clientChannel) throws IOException {
        StringBuilder userListMsg = new StringBuilder("CHAT_USERS|");
        for (String username : clients.values()) {
            userListMsg.append(username).append(",");
        }
        sendToClient(clientChannel, userListMsg.toString());
    }
    
    private void broadcastUserList() throws IOException {
        StringBuilder userListMsg = new StringBuilder("CHAT_USERS|");
        for (String username : clients.values()) {
            userListMsg.append(username).append(",");
        }
        
        String message = userListMsg.toString();
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        
        for (SocketChannel client : clients.keySet()) {
            if (client.isConnected()) {
                buffer.rewind();
                client.write(buffer);
            }
        }
    }

    public static void start() {
        try {
            ChatManager chatManager = new ChatManager();
            new Thread(chatManager, "ChatManager-NIO").start();
        } catch (IOException e) {
            System.err.println("Failed to start Chat Manager: " + e.getMessage());
        }
    }
}
