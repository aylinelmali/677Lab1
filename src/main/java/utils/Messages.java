package utils;

import product.Product;

import java.awt.*;

public class Messages {
    public static String getLookupForwardMessage(int buyerID, Product product, int forwardPeerID) {
        return "Lookup request from buyer " + buyerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getLookupArrivedMessage(int buyerID, Product product, int sellerID) {
        return "Lookup request from buyer " + buyerID + " for " + product + " arrived at seller " + sellerID;
    }

    public static String getLookupDropped(int buyerID, Product product, int peerID) {
        return "Lookup from buyer " + buyerID + " for " + product + " dropped by peer " + peerID;
    }

    public static String getReplyForwardMessage(int sellerID, Product product, int forwardPeerID) {
        return "Reply from seller " + sellerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getReplyArrivedMessage(int sellerID, Product product, int buyerID) {
        return "Reply from seller " + sellerID + " for " + product + " arrived at buyer " + buyerID;
    }

    public static String getWrongReplyMessage(int sellerID, Product product, int buyerID) {
        return "Reply from seller " + sellerID + " with wrong product " + product + " arrived at buyer " + buyerID;
    }

    public static String getBuyForwardMessage(int buyerID, Product product, int forwardPeerID) {
        return "Buy request from buyer " + buyerID + " for " + product + " forwarded by peer " + forwardPeerID;
    }

    public static String getBuyArrivedMessage(int buyerID, Product product, int sellerID) {
        return "Buy request from buyer " + buyerID + " for " + product + " arrived at seller " + sellerID;
    }

    public static String getBoughtMessage(int sellerID, Product product, int stock) {
        return "Bought " + product + " from seller " + sellerID + ". Remaining stock: " + stock;
    }

    public static String getOutOfStockMessage(int sellerID, Product product) {
        return "Seller " + sellerID + " has no more stock for product " + product;
    }

    public static String getWrongProductMessage(int buyerID, Product product, int sellerID) {
        return "Buyer " + buyerID + " contacted seller " + sellerID + " with wrong product " + product;
    }

    public static String getForwardErrorMessage() {
        return "Error: Couldn't forward message.";
    }
}
