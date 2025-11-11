package server;

import java.io.Serializable;

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
}