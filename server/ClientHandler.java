package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import shared.MessageSender;
import shared.Protocol;

public class ClientHandler implements Runnable, MessageSender {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private List<MessageSender> senders;

    public ClientHandler(Socket socket, List<MessageSender> senders) {
        this.socket = socket;
        this.senders = senders;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Login
            String loginMsg = in.readUTF();
            if (loginMsg.startsWith(Protocol.LOGIN + "|")) {
                username = loginMsg.split("\\|")[1];
                out.writeUTF(Protocol.welcomeMessage(username));
                Server.broadcast(Protocol.welcomeMessage(username)); // optional
            } else {
                out.writeUTF(Protocol.errorMessage("Invalid login"));
                return;
            }

            String input;
            while ((input = in.readUTF()) != null) {
                AuctionManager.processMessage(input, this);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
            senders.remove(this);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send to " + username);
            senders.remove(this);
        }
    }

    @Override
    public String getUsername() { return username; }
}