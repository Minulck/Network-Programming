package server.auction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private double startPrice;
    private double currentBid;
    private String highestBidder;
    private int durationSec;
    private String creator;
    private long creationTime;
    private int bidCount;
    private long lastBidTime;
    private List<Bid> bidHistory;
    private String imageName;

    public static class Bid {
        private String bidder;
        private double amount;
        private long timestamp;

        public Bid(String bidder, double amount, long timestamp) {
            this.bidder = bidder;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public String getBidder() { return bidder; }
        public double getAmount() { return amount; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return bidder + "," + amount + "," + timestamp;
        }
    }

    public Auction(int id, String name, double startPrice, int durationSec, String creator) {
        this.id = id;
        this.name = name;
        this.startPrice = startPrice;
        this.currentBid = startPrice;
        this.highestBidder = null;
        this.durationSec = durationSec;
        this.creator = creator;
        this.creationTime = System.currentTimeMillis();
        this.bidCount = 0;
        this.lastBidTime = 0;
        this.bidHistory = new ArrayList<>();
        this.imageName = null;
    }
    
    public Auction(int id, String name, double startPrice, int durationSec, String creator, String imageName) {
        this(id, name, startPrice, durationSec, creator);
        this.imageName = imageName;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getStartPrice() { return startPrice; }
    public double getCurrentBid() { return currentBid; }
    public void setCurrentBid(double currentBid) { 
        this.currentBid = currentBid; 
        this.bidCount++;
        this.lastBidTime = System.currentTimeMillis();
    }
    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public int getDurationSec() { return durationSec; }
    public String getCreator() { return creator; }
    public long getCreationTime() { return creationTime; }
    public int getBidCount() { return bidCount; }
    public long getLastBidTime() { return lastBidTime; }
    public List<Bid> getBidHistory() { return new ArrayList<>(bidHistory); }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public void addBidToHistory(String bidder, double amount) {
        bidHistory.add(new Bid(bidder, amount, System.currentTimeMillis()));
    }
}