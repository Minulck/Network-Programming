package server;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Feature 2: UDP Broadcast for Auction Notifications
 * Demonstrates: DatagramSocket, DatagramPacket, UDP Broadcasting
 * Contributor: Member 2
 */
public class UDPNotificationService {
    private static final int UDP_PORT = 5003;
    private static DatagramSocket socket;
    private static List<InetAddress> subscribers = new CopyOnWriteArrayList<>();
    private static boolean running = true;

    public static void start() {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            System.out.println("UDP Notification Service started");
            
            // Start listener for subscription requests
            new Thread(() -> listenForSubscribers()).start();
        } catch (SocketException e) {
            System.err.println("Failed to start UDP Notification Service: " + e.getMessage());
        }
    }

    private static void listenForSubscribers() {
        try (DatagramSocket listenerSocket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Subscription listener started on port " + UDP_PORT);
            byte[] buffer = new byte[256];
            
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                listenerSocket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("SUBSCRIBE")) {
                    InetAddress clientAddress = packet.getAddress();
                    if (!subscribers.contains(clientAddress)) {
                        subscribers.add(clientAddress);
                        System.out.println("New UDP subscriber: " + clientAddress.getHostAddress());
                        
                        // Send confirmation
                        sendToSubscriber(clientAddress, "SUBSCRIBED|You will receive auction notifications");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("UDP Listener error: " + e.getMessage());
        }
    }

    private static void sendToSubscriber(InetAddress address, String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, UDP_PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send to subscriber: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            byte[] data = message.getBytes();
            
            // Send to all subscribers
            for (InetAddress subscriber : subscribers) {
                DatagramPacket packet = new DatagramPacket(data, data.length, subscriber, UDP_PORT);
                socket.send(packet);
            }
            
            // Also send to broadcast address
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket broadcastPacket = new DatagramPacket(data, data.length, broadcastAddress, UDP_PORT);
            socket.send(broadcastPacket);
            
            System.out.println("UDP Broadcast sent: " + message);
        } catch (IOException e) {
            System.err.println("UDP Broadcast failed: " + e.getMessage());
        }
    }

    // Notification types
    public static void notifyNewAuction(String auctionName, double startPrice) {
        String message = String.format("NEW_AUCTION|%s|Starting at $%.2f", auctionName, startPrice);
        broadcast(message);
    }

    public static void notifyBiddingWar(int auctionId, String auctionName, int bidCount) {
        String message = String.format("BIDDING_WAR|Auction #%d (%s)|%d bids in last minute!", 
                                      auctionId, auctionName, bidCount);
        broadcast(message);
    }

    public static void notifyEndingSoon(int auctionId, String auctionName, int secondsLeft) {
        String message = String.format("ENDING_SOON|Auction #%d (%s)|Only %d seconds left!", 
                                      auctionId, auctionName, secondsLeft);
        broadcast(message);
    }

    public static void notifyAuctionEnded(int auctionId, String winner, double finalPrice) {
        String message = String.format("AUCTION_ENDED|Auction #%d|Winner: %s|Price: $%.2f", 
                                      auctionId, winner, finalPrice);
        broadcast(message);
    }

    public static void notifyHighBid(int auctionId, String bidder, double amount) {
        String message = String.format("HIGH_BID|Auction #%d|%s bid $%.2f", 
                                      auctionId, bidder, amount);
        broadcast(message);
    }

    public static void shutdown() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
