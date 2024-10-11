package peer;

import product.Product;

import java.rmi.Remote;
import java.util.List;

public interface IPeer extends Remote {

    int MAX_HOP_COUNT = 3;

    void start();
    void lookup(int buyerID, Product product, int hopCount, int[] searchPath);
    void reply(int sellerID, int[] replyPath);
    void buy(int peerID,int[] path);
    int getPeerID();
    List<IPeer> getNeighbors();
}
