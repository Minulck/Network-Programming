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
}
