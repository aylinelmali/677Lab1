package peer;

import product.Product;
import utils.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Seller extends APeer {

    private int itemStock;
    private Product productType;
    private final Lock stockLock = new ReentrantLock();

    public Seller(int peerID, List<Integer> neighbors, Registry registry, Product product) throws RemoteException {
        this(peerID, neighbors, registry);
        this.productType = product;
    }

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
            int[] newSearchPath = getNewSearchPath(searchPath);
            reply(peerID, newSearchPath);
            Logger.log("Lookup request from buyer " + buyerID + " for " + product + " - Seller found: " + peerID);
        }
        else{
            forward(product, hopCount, searchPath);
            Logger.log("Lookup request from buyer " + buyerID + " for " + product + " forwarded by peer " + peerID);
        }
    }

    @Override
    public void reply(int sellerID, int[] replyPath) throws RemoteException {
        int peerIndex = getPeerIndex(replyPath);
        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path
            IPeer peer = getNeighbors().get(replyPath[peerIndex - 1]);
            if (peer != null) {
                peer.reply(sellerID, replyPath);
            } else {
                Logger.log("Error: Couldn't forward message.");
            }
        }
    }

    @Override
    public synchronized void buy(int[] path) throws RemoteException {
        int sellerID = path[path.length - 1];
        if (sellerID == this.peerID) {
            stockLock.lock();
            try {
                if (decrementStock()) {
                    Logger.log("Bought " + productType + " from seller " + peerID + ". Remaining stock: " + itemStock);
                    if (itemStock <= 0) {
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
        } else {
            int peerIndex = getPeerIndex(path);
            IPeer neighbor = getNeighbors().get(path[peerIndex + 1]);
            if (neighbor != null) {
                neighbor.buy(path);
                Logger.log("Buy request from buyer " + path[0] + "forwarded by peer " + peerID);
            } else {
               Logger.log("Error: Couldn't forward message.");
            }
        }
    }
}
