package peer;

import product.Product;
import utils.Logger;
import utils.Messages;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;

public class Seller extends APeer {

    private int itemStock;
    private Product productType;

    private Map<Integer, Integer> sequenceNumbers;

    public Seller(int peerID, List<Integer> neighbors, Registry registry, Product product) throws RemoteException {
        this(peerID, neighbors, registry);
        this.productType = product;
    }

    public Seller(int peerID, List<Integer> neighbors, Registry registry) throws RemoteException {
        super(peerID, neighbors, registry);
        this.itemStock = 5 + new Random().nextInt(10);
        this.productType = Product.pickRandomProduct();
    }

    /**
     * Decrements the stock.
     * @return if <code>itemStock > 0</code>, decrement the stock and return <code>true</code>, else return <code>false</code>.
     */
    private synchronized boolean decrementStock() {
        if (itemStock > 0) {
            itemStock--;
            return true;
        }
        return false;
    }

    @Override
    public void start() throws RemoteException { // method unnecessary for the seller, only buyer uses this.
        this.sequenceNumbers = new HashMap<>();
        Logger.log("Peer " + peerID + " (Seller), " + "Product: " + productType + ", Stock: " + itemStock);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath, int sequenceNumber) throws RemoteException {
        if (this.productType.equals(product)) { // seller sells the product
            synchronized (this) {
                // checks that sequence number is not used yet.
                if (sequenceNumbers.containsKey(buyerID) && sequenceNumbers.get(buyerID) >= sequenceNumber) {
                    return;
                }
                if (itemStock > 0) { // product is in stock
                    sequenceNumbers.put(buyerID, sequenceNumber);
                    int[] newSearchPath = getNewSearchPath(searchPath);
                    Logger.log(Messages.getLookupArrivedMessage(buyerID, product, peerID));
                    reply(peerID, product, newSearchPath);
                    return;
                }
            }
        }
        // seller doesn't have the product in stock, forward the message to the next peer.
        forward(buyerID, product, hopCount, searchPath, sequenceNumber);
    }

    @Override
    public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {
        int peerIndex = getPeerIndex(replyPath);
        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path.
            IPeer peer = getNeighbors().get(replyPath[peerIndex - 1]);
            if (peer != null) {
                Logger.log(Messages.getReplyForwardMessage(sellerID, product, peerID));
                peer.reply(sellerID, product, replyPath);
            } else {
                Logger.log(Messages.getForwardErrorMessage());
            }
        }
    }

    @Override
    public void buy(Product product, int[] path) throws RemoteException {
        int sellerID = path[path.length - 1];
        if (sellerID == this.peerID) { // message arrived at seller.
            Logger.log(Messages.getBuyArrivedMessage(path[0], product, peerID));
            synchronized (this) {
                if (product != this.productType) { // incorrect product type, do not decrement.
                    Logger.log(Messages.getWrongProductMessage(path[0], product, sellerID));
                } else if (decrementStock()) { // correct product type. Decrement stock if possible, else, pick new random product.
                    Logger.log(Messages.getBoughtMessage(sellerID, product, itemStock));
                    ack(sellerID, product, path);
                    if (itemStock <= 0) {
                        this.productType = Product.pickRandomProduct();
                        this.itemStock = 5 + new Random().nextInt(10);
                    }
                } else {
                    Logger.log(Messages.getOutOfStockMessage(sellerID, product));
                }
            }
        } else { // message not arrived at seller, forward to next peer.
            int peerIndex = getPeerIndex(path);
            IPeer neighbor = getNeighbors().get(path[peerIndex + 1]);
            if (neighbor != null) {
                Logger.log(Messages.getBuyForwardMessage(path[0], product, peerID));
                neighbor.buy(product, path);
            } else {
               Logger.log(Messages.getForwardErrorMessage());
            }
        }
    }

    @Override
    public void ack(int sellerID, Product product, int[] path) throws RemoteException {
        int peerIndex = getPeerIndex(path);
        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path.
            IPeer peer = getNeighbors().get(path[peerIndex - 1]);
            if (peer != null) {
                Logger.log(Messages.getAckForwardMessage(sellerID, product, peerID));
                peer.ack(sellerID, product, path);
            } else {
                Logger.log(Messages.getForwardErrorMessage());
            }
        }
    }
}
