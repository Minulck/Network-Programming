package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
                        System.out.println("Update: " + msg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected");
                }
            }).start();

            // Send commands
            while (true) {
                System.out.print("Command (CREATE name price duration or BID id amount): ");
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