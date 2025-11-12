package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import shared.MessageSender;
import shared.Protocol;

public class AuctionManager {
    private static List<Auction> auctions = new ArrayList<>();
    private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private static Map<Integer, Long> auctionStartTimes = new ConcurrentHashMap<>();
    private static Map<Integer, Integer> recentBids = new ConcurrentHashMap<>();

    public static void processMessage(String message, MessageSender handler) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        switch (type) {
            case Protocol.CREATE:
                if (parts.length == 4) {
                    String name = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int durationSec = Integer.parseInt(parts[3]);
                    createAuction(name, startPrice, durationSec, handler, null);
                } else if (parts.length == 5) {
                    String name = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int durationSec = Integer.parseInt(parts[3]);
                    String imageName = parts[4];
                    createAuction(name, startPrice, durationSec, handler, imageName);
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
            case Protocol.GET_BID_HISTORY:
                if (parts.length == 2) {
                    int auctionId = Integer.parseInt(parts[1]);
                    getBidHistory(auctionId, handler);
                }
                break;
            default:
                handler.sendMessage(Protocol.errorMessage("Unknown command"));
        }
    }

    public static void createAuction(String name, double startPrice, int durationSec, MessageSender creator, String imageName) {
        Auction auction = new Auction(auctions.size(), name, startPrice, durationSec, creator.getUsername(), imageName);
        auctions.add(auction);
        auctionStartTimes.put(auction.getId(), System.currentTimeMillis());
        recentBids.put(auction.getId(), 0);

        // Send standard update message with image
        Server.broadcast(Protocol.newAuctionMessage(auction.getId(), auction.getName(), auction.getStartPrice(), auction.getDurationSec(), auction.getImageName()));

        // UDP Notification: New auction created
        UDPNotificationService.notifyNewAuction(auction.getName(), startPrice);
        
        // Send rich notification with emoji to all clients
        Server.broadcast(Protocol.auctionStartNotification(auction.getId(), auction.getName(), auction.getStartPrice(), creator.getUsername()));

        // Schedule auto-end
        timer.schedule(() -> endAuction(auction), durationSec, TimeUnit.SECONDS);
        
        // Schedule "ending soon" notification (10 seconds before end)
        if (durationSec > 10) {
            timer.schedule(() -> {
                UDPNotificationService.notifyEndingSoon(auction.getId(), auction.getName(), 10);
            }, durationSec - 10, TimeUnit.SECONDS);
        }
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
            
            // Send standard update message
            a.addBidToHistory(bidder, amount);
            Server.broadcast(Protocol.updateMessage(a.getId(), a.getCurrentBid(), a.getHighestBidder()));
            
            // Send rich bid notification with emoji to all clients
            Server.broadcast(Protocol.bidNotification(a.getId(), a.getName(), amount, bidder));
            
            // UDP Notification: High bid
            UDPNotificationService.notifyHighBid(auctionId, bidder, amount);
            
            // Track recent bids for bidding war detection
            recentBids.merge(auctionId, 1, Integer::sum);
            
            // Check for bidding war (5+ bids in quick succession)
            if (recentBids.get(auctionId) >= 5) {
                UDPNotificationService.notifyBiddingWar(auctionId, a.getName(), recentBids.get(auctionId));
                recentBids.put(auctionId, 0); // Reset counter
            }
            
            // Reset bidding war counter after 60 seconds
            timer.schedule(() -> recentBids.put(auctionId, 0), 60, TimeUnit.SECONDS);
            
        } else {
            handler.sendMessage(Protocol.errorMessage("Bid too low"));
        }
    }

    private static void endAuction(Auction a) {
        String winner = a.getHighestBidder() != null ? a.getHighestBidder() : "No bids";
        double finalPrice = a.getCurrentBid();
        
        // Send standard end message
        Server.broadcast(Protocol.endAuctionMessage(a.getId(), winner, finalPrice));
        
        // UDP Notification: Auction ended
        UDPNotificationService.notifyAuctionEnded(a.getId(), winner, finalPrice);
        
        
        // Send rich auction end notification with emoji to all clients
        Server.broadcast(Protocol.auctionEndNotification(a.getId(), a.getName(), winner, finalPrice));
        // Optional: remove from list
    }

    public static void getBidHistory(int auctionId, MessageSender handler) {
        if (auctionId < 0 || auctionId >= auctions.size()) {
            handler.sendMessage(Protocol.errorMessage("Invalid auction ID"));
            return;
        }
        Auction a = auctions.get(auctionId);
        if (a == null) {
            handler.sendMessage(Protocol.errorMessage("Auction not found"));
            return;
        }
        List<Auction.Bid> history = a.getBidHistory();
        StringBuilder csv = new StringBuilder("Bidder,Amount,Timestamp\n");
        for (Auction.Bid bid : history) {
            csv.append(bid.getBidder()).append(",").append(bid.getAmount()).append(",").append(bid.getTimestamp()).append("\n");
        }
        handler.sendMessage(Protocol.bidHistoryMessage(auctionId, csv.toString()));
    }
    
    public static List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions);
    }
}