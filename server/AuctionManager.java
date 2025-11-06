package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import shared.MessageSender;
import shared.Protocol;

public class AuctionManager {
    private static List<Auction> auctions = new ArrayList<>();
    private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

    public static void processMessage(String message, MessageSender handler) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        switch (type) {
            case Protocol.CREATE:
                if (parts.length == 4) {
                    String name = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int durationSec = Integer.parseInt(parts[3]);
                    createAuction(name, startPrice, durationSec, handler);
                }
                break;
            case Protocol.BID:
                if (parts.length == 4) {
                    int auctionId = Integer.parseInt(parts[1]);
                    double amount = Double.parseDouble(parts[2]);
                    String bidder = parts[3];
                    placeBid(auctionId, amount, bidder, handler);
                }
                break;
            default:
                handler.sendMessage(Protocol.errorMessage("Unknown command"));
        }
    }

    public static void createAuction(String name, double startPrice, int durationSec, MessageSender creator) {
        Auction auction = new Auction(auctions.size(), name, startPrice, durationSec, creator.getUsername());
        auctions.add(auction);

        Server.broadcast(Protocol.newAuctionMessage(auction.getId(), auction.getName(), auction.getStartPrice(), auction.getDurationSec()));

        // Schedule auto-end
        timer.schedule(() -> endAuction(auction), durationSec, TimeUnit.SECONDS);
    }

    public static void placeBid(int auctionId, double amount, String bidder, MessageSender handler) {
        if (auctionId < 0 || auctionId >= auctions.size()) {
            handler.sendMessage(Protocol.errorMessage("Invalid auction ID"));
            return;
        }
        Auction a = auctions.get(auctionId);
        if (a != null && amount > a.getCurrentBid()) {
            a.setCurrentBid(amount);
            a.setHighestBidder(bidder);
            Server.broadcast(Protocol.updateMessage(a.getId(), a.getCurrentBid(), a.getHighestBidder()));
        } else {
            handler.sendMessage(Protocol.errorMessage("Bid too low"));
        }
    }

    private static void endAuction(Auction a) {
        String winner = a.getHighestBidder() != null ? a.getHighestBidder() : "No bids";
        double finalPrice = a.getCurrentBid();
        Server.broadcast(Protocol.endAuctionMessage(a.getId(), winner, finalPrice));
        // Optional: remove from list
    }
}