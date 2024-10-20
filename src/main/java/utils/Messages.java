package utils;

import product.Product;

import java.util.List;

public class Messages {
    public static String getLookupForwardMessage(int buyerID, Product product, int forwardPeerID) {
        return buyerID == forwardPeerID ?
            "Lookup from buyer " + buyerID + " for " + product + " initiated" :
            "Lookup from buyer " + buyerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getLookupArrivedMessage(int buyerID, Product product, int sellerID) {
        return "Lookup request from buyer " + buyerID + " for " + product + " arrived at seller " + sellerID;
    }

    public static String getLookupDropped(int buyerID, Product product, int peerID) {
        return "Lookup from buyer " + buyerID + " for " + product + " dropped by peer " + peerID;
    }

    public static String getReplyForwardMessage(int sellerID, Product product, int forwardPeerID) {
        return sellerID == forwardPeerID ?
                "Reply from seller " + sellerID + " for " + product + " initiated" :
                "Reply from seller " + sellerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getReplyArrivedMessage(int sellerID, Product product, int buyerID) {
        return "Reply from seller " + sellerID + " for " + product + " arrived at buyer " + buyerID;
    }

    public static String getWrongReplyMessage(int sellerID, Product product, int buyerID) {
        return "Reply from seller " + sellerID + " with wrong product " + product + " arrived at buyer " + buyerID;
    }

    public static String getBuyForwardMessage(int buyerID, Product product, int forwardPeerID) {
        return buyerID == forwardPeerID ?
                "Buy request from buyer " + buyerID + " for " + product + " initiated" :
                "Buy request from buyer " + buyerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getBuyArrivedMessage(int buyerID, Product product, int sellerID) {
        return "Buy request from buyer " + buyerID + " for " + product + " arrived at seller " + sellerID;
    }

    public static String getBoughtMessage(int sellerID, Product product, int stock) {
        return "Bought " + product + " from seller " + sellerID + ". Remaining stock: " + stock;
    }

    public static String getAckForwardMessage(int sellerID, Product product, int forwardPeerID) {
        return sellerID == forwardPeerID ?
                "Ack from seller " + sellerID + " for " + product + " initiated" :
                "Ack from seller " + sellerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getAckArrivedMessage(int sellerID, Product product, int buyerID) {
        return "Ack from seller " + sellerID + " for " + product + " arrived at buyer " + buyerID;
    }

    public static String getOutOfStockMessage(int sellerID, Product product) {
        return "Seller " + sellerID + " has no more stock for product " + product;
    }

    public static String getWrongProductMessage(int buyerID, Product product, int sellerID) {
        return "Buyer " + buyerID + " contacted seller " + sellerID + " with wrong product " + product;
    }

    public static String getStatisticsMessage(int peerID, List<Long> deltas) {
        return "Peer " + peerID + " has an average response time of " + deltas.stream().mapToLong(Long::longValue).average().orElse(0) + ". Deltas: " + deltas;
    }

    public static String getForwardErrorMessage() {
        return "Error: Couldn't forward message!";
    }
}
