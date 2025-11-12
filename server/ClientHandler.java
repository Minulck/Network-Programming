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
                out.flush();
                
                System.out.println("User " + username + " logged in. Sending existing auctions...");
                
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
                    System.out.println("Sending auction to " + username + ": " + auctionMsg);
                    out.writeUTF(auctionMsg);
                    out.flush();
                    
                    // If there's already a bid on this auction, send an update
                    if (auction.getHighestBidder() != null) {
                        String updateMsg = Protocol.updateMessage(
                            auction.getId(), 
                            auction.getCurrentBid(), 
                            auction.getHighestBidder()
                        );
                        System.out.println("Sending update to " + username + ": " + updateMsg);
                        out.writeUTF(updateMsg);
                        out.flush();
                    }
                }
                
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