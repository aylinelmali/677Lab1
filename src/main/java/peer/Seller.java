package peer;

import product.Product;
import utils.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Seller extends APeer {

    private int itemStock;
    private Product productType;
    private List<IPeer> neighborPeers;
    private Registry registry;
    private final Lock stockLock = new ReentrantLock();

    public Seller(int peerID, List<Integer> neighbors, Registry registry) throws RemoteException {
        super(peerID, neighbors, registry);
        this.itemStock = new Random().nextInt(1, 6);
        this.productType = Product.pickRandomProduct();
    }

    public synchronized boolean decrementStock() {
        stockLock.lock();
        try{
            if (itemStock > 0) {
                itemStock--;
                return true;
            }
            return false;
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public void start() throws RemoteException {

    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {
        if(this.productType.equals(product) && itemStock > 0){
            reply(peerID, searchPath);
            Logger.log("Lookup request from buyer " + buyerID + " for " + product + " - Seller found: " + peerID);
        }
        else{
            forward(buyerID, product, hopCount, searchPath);
            Logger.log("Lookup request from buyer " + buyerID + " for " + product + " forwarded by peer " + peerID);
        }
    }

    @Override
    public void reply(int sellerID, int[] replyPath) throws RemoteException {
        int peerIndex = getPeerIndex(replyPath);
        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path
            getNeighbors().get(peerIndex - 1).reply(sellerID, replyPath);
        }
    }

    @Override
    public synchronized void buy(int peerID, int[] path) throws RemoteException {
        stockLock.lock();
        try {
            if (decrementStock()) {
                String logMessage = "Bought " + productType + " from seller " + peerID + ". Remaining stock: " + itemStock;
                Logger.log(logMessage);
                if(itemStock <= 0){
                    Random rand = new Random();
                    this.productType = Product.pickRandomProduct();
                    this.itemStock = 5 + rand.nextInt(10);
                }
            } else {
                Logger.log("Seller " + peerID + " is out of stock! Cannot complete the transaction.");
            }
        } finally {
            stockLock.unlock();
        }
    }
}
