package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IPeer extends Remote {

    /**
     * Initializes and starts the peer.
     */
    void start() throws RemoteException;

    /**
     * If this peer is not the correct seller, broadcasts the lookup message to all surrounding peers, else, initiate a reply.
     * @param buyerID The buyer that initiated the lookup.
     * @param product The product to lookup
     * @param hopCount The current hop count. If <code>hopCount <= 0</code>, the lookup will be discarded.
     * @param searchPath The travelled path of the lookup.
     * @param sequenceNumber Unique id for the buyer's request.
     */
    void lookup(int buyerID, Product product, int hopCount, int[] searchPath, int sequenceNumber) throws RemoteException;

    /**
     * Sends a reply to the buyer that called the lookup method.
     * @param sellerID The seller with the correct product that initiated the reply.
     * @param product The product of the seller.
     * @param replyPath The travelled path of the lookup. Used to find the buyer who initiated the lookup
     */
    void reply(int sellerID, Product product, int[] replyPath) throws RemoteException;

    /**
     * Sends a buy message to the seller.
     * @param product Product to buy.
     * @param path The travelled path of the lookup. Used to find the seller of the product.
     */
    void buy(Product product, int[] path) throws RemoteException;

    /**
     * Acknowledges the buy message.
     * @param sellerID The seller that acknowledges the buy message.
     * @param product The bought product.
     * @param path The travelled path of the lookup. Used to find the buyer of the product.
     */
    void ack(int sellerID, Product product, int[] path) throws RemoteException;

    /**
     * @return The ID of the peer.
     */
    int getPeerID() throws RemoteException;

    /**
     * @return All <b>direct</b> neighbors.
     */
    Map<Integer, IPeer> getNeighbors() throws RemoteException;
}
