package peer;

import product.Product;

import java.util.List;

public class Buyer extends APeer {

    public Buyer(int peerID, List<Integer> neighbors) {
        super(peerID, neighbors);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, List<Integer> searchPath) {

    }

    @Override
    public void reply(int sellerID, List<Integer> replyPath) {

    }

    @Override
    public void buy(int peerID, List<Integer> path) {

    }

    @Override
    public void run() {

    }
}
