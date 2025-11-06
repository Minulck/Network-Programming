package server;

public class Auction {
    private int id;
    private String name;
    private double startPrice;
    private double currentBid;
    private String highestBidder;
    private int durationSec;
    private String creator;

    public Auction(int id, String name, double startPrice, int durationSec, String creator) {
        this.id = id;
        this.name = name;
        this.startPrice = startPrice;
        this.currentBid = startPrice;
        this.highestBidder = null;
        this.durationSec = durationSec;
        this.creator = creator;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getStartPrice() { return startPrice; }
    public double getCurrentBid() { return currentBid; }
    public void setCurrentBid(double currentBid) { this.currentBid = currentBid; }
    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public int getDurationSec() { return durationSec; }
    public String getCreator() { return creator; }
}