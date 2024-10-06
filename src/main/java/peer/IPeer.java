package peer;

import product.Product;

import java.util.List;

public interface IPeer extends Runnable {
    void lookup(int buyerID, Product product, int hopCount, List<Integer> searchPath);
    void reply(int sellerID, List<Integer> replyPath);
    void buy(int peerID, List<Integer> path);
    int getPeerID();
    List<Integer> getNeighbors();
}
