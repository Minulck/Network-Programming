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
}