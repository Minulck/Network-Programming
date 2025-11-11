package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import shared.Protocol;


public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            out.writeUTF(Protocol.loginMessage(username));
            out.flush();

            String response = in.readUTF();
            System.out.println("Server: " + response);

            // Start a thread to listen for messages
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readUTF()) != null) {
                        if (msg.startsWith(Protocol.BID_HISTORY + "|")) {
                            String[] parts = msg.split("\\|", 3);
                            if (parts.length >= 3) {
                                int auctionId = Integer.parseInt(parts[1]);
                                String csvData = parts[2];
                                try (FileWriter writer = new FileWriter("bid_history_" + auctionId + ".csv")) {
                                    writer.write(csvData);
                                    System.out.println("Bid history for auction " + auctionId + " saved to bid_history_" + auctionId + ".csv");
                                } catch (IOException e) {
                                    System.out.println("Failed to save bid history: " + e.getMessage());
                                }
                            }
                        } else {
                            System.out.println("Update: " + msg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected");
                }
            }).start();

            // Send commands
            while (true) {
                System.out.print("Command (CREATE name price duration, BID id amount, GET_BID_HISTORY id): ");
                String command = scanner.nextLine();
                if (command.equals("exit")) break;
                out.writeUTF(command);
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handle and display server messages with proper formatting
     */
    private static void handleServerMessage(String msg) {
        String[] parts = msg.split("\\|", 2);
        String messageType = parts[0];
        
        switch (messageType) {
            case Protocol.AUCTION_START_NOTIFICATION:
            case Protocol.BID_NOTIFICATION:
            case Protocol.AUCTION_END_NOTIFICATION:
            case Protocol.NOTIFICATION:
                // Display rich notifications with special formatting
                System.out.println("\n╔════════════════════════════════════════════════════════════╗");
                System.out.println("║ " + (parts.length > 1 ? parts[1] : msg));
                System.out.println("╚════════════════════════════════════════════════════════════╝");
                break;
            
            case Protocol.NEW_AUCTION:
                // Standard auction creation message
                if (parts.length > 1) {
                    String[] auctionData = parts[1].split("\\|");
                    if (auctionData.length >= 4) {
                        System.out.println("\n[INFO] Auction #" + auctionData[0] + " - " + auctionData[1] + 
                                         " starting at Rs." + auctionData[2] + " for " + auctionData[3] + " seconds");
                    }
                }
                break;
            
            case Protocol.UPDATE:
                // Bid update message
                if (parts.length > 1) {
                    String[] bidData = parts[1].split("\\|");
                    if (bidData.length >= 3) {
                        System.out.println("[UPDATE] Auction #" + bidData[0] + " - Current bid: Rs." + 
                                         bidData[1] + " by " + bidData[2]);
                    }
                }
                break;
            
            case Protocol.END:
                // Auction end message
                if (parts.length > 1) {
                    String[] endData = parts[1].split("\\|");
                    if (endData.length >= 3) {
                        System.out.println("[END] Auction #" + endData[0] + " - Winner: " + 
                                         endData[1] + " at Rs." + endData[2]);
                    }
                }
                break;
            
            case Protocol.ERROR:
                // Error message
                System.out.println("\n❌ ERROR: " + (parts.length > 1 ? parts[1] : "Unknown error"));
                break;
            
            case Protocol.WELCOME:
                // Welcome message
                System.out.println("\n✅ " + (parts.length > 1 ? "Welcome " + parts[1] : msg));
                break;
            
            default:
                // Fallback for unknown message types
                System.out.println("[Server] " + msg);
                break;
        }
        
        // Print command prompt again for better UX
        System.out.print("\nCommand (CREATE name price duration or BID id amount): ");
    }
}