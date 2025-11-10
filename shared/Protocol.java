package shared;
public class Protocol {
    // Message types
    public static final String LOGIN = "LOGIN";
    public static final String CREATE = "CREATE";
    public static final String BID = "BID";
    public static final String UPDATE = "UPDATE";
    public static final String NEW_AUCTION = "NEW_AUCTION";
    public static final String END = "END";
    public static final String ERROR = "ERROR";
    public static final String WELCOME = "WELCOME";
    
    // Notification message types (for enhanced server push)
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String BID_NOTIFICATION = "BID_NOTIFICATION";
    public static final String AUCTION_START_NOTIFICATION = "AUCTION_START_NOTIFICATION";
    public static final String AUCTION_END_NOTIFICATION = "AUCTION_END_NOTIFICATION";

    // Helper methods for message formatting
    public static String loginMessage(String username) {
        return LOGIN + "|" + username;
    }

    public static String createMessage(String name, double startPrice, int durationSec) {
        return CREATE + "|" + name + "|" + startPrice + "|" + durationSec;
    }

    public static String bidMessage(int auctionId, double amount, String username) {
        return BID + "|" + auctionId + "|" + amount + "|" + username;
    }

    public static String updateMessage(int auctionId, double currentBid, String bidder) {
        return UPDATE + "|" + auctionId + "|" + currentBid + "|" + bidder;
    }

    public static String newAuctionMessage(int id, String name, double startPrice, int duration) {
        return NEW_AUCTION + "|" + id + "|" + name + "|" + startPrice + "|" + duration;
    }

    public static String endAuctionMessage(int auctionId, String winner, double finalPrice) {
        return END + "|" + auctionId + "|" + winner + "|" + finalPrice;
    }

    public static String errorMessage(String message) {
        return ERROR + "|" + message;
    }

    public static String welcomeMessage(String username) {
        return WELCOME + "|" + username;
    }
    
    // Notification helper methods with emojis
    public static String auctionStartNotification(int id, String name, double startPrice, String creator) {
        return AUCTION_START_NOTIFICATION + "|🔔 New auction started: " + name + 
               " at Rs. " + String.format("%.2f", startPrice) + " by " + creator + " (ID: " + id + ")";
    }
    
    public static String bidNotification(int auctionId, String name, double amount, String bidder) {
        return BID_NOTIFICATION + "|⚡ New highest bid on '" + name + "': Rs. " + 
               String.format("%.2f", amount) + " by " + bidder + " (Auction ID: " + auctionId + ")";
    }
    
    public static String auctionEndNotification(int auctionId, String name, String winner, double finalPrice) {
        if (winner.equals("No bids")) {
            return AUCTION_END_NOTIFICATION + "|🏁 Auction ended: '" + name + "' - No bids received (ID: " + auctionId + ")";
        }
        return AUCTION_END_NOTIFICATION + "|🏁 Auction ended: '" + name + "' - Winner: " + winner + 
               " at Rs. " + String.format("%.2f", finalPrice) + " (ID: " + auctionId + ")";
    }
    
    public static String generalNotification(String message) {
        return NOTIFICATION + "|" + message;
    }
}
