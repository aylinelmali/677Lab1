package peer;

import product.Product;
import utils.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer {

    private final List<ReplyPath> sellers;

    public Buyer(int peerID, List<Integer> neighbors, Registry registry) throws RemoteException {
        super(peerID, neighbors, registry);

        sellers = new ArrayList<>();
    }

    @Override
    public void start() throws RemoteException {
        buyNewProduct();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (sellers.isEmpty()) {
                return;
            }

            ReplyPath seller = sellers
                    .stream()
                    .skip((int) (sellers.size() * Math.random()))
                    .findFirst()
                    .orElseThrow();
            sellers.clear();

            try {
                buy(seller.replyPath);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(new Random().nextInt(1,3) * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                buyNewProduct(); // restart the cycle
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {
        forward(product, hopCount, searchPath);
        Logger.log("Lookup request from buyer " + buyerID + " for " + product + " forwarded by peer " + peerID);
    }

    @Override
    public void reply(int sellerID, int[] replyPath) throws RemoteException {
        int peerIndex = getPeerIndex(replyPath);

        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path
            IPeer peer = getNeighbors().get(replyPath[peerIndex - 1]);
            if (peer != null) {
                peer.reply(sellerID, replyPath);
            }
        } else if (peerIndex == 0) { // this peer is the original buyer
            sellers.add(new ReplyPath(replyPath));
        }
    }

    @Override
    public void buy(int[] path) throws RemoteException {
        int peerIndex = getPeerIndex(path);
        IPeer peer = getNeighbors().get(path[peerIndex + 1]);
        if (peer != null) {
            peer.buy(path);
            Logger.log("Buy request from buyer " + path[0] + "forwarded by peer " + peerID);
        } else {
            Logger.log("Error: Couldn't forward message.");
        }
    }

    private void buyNewProduct() throws RemoteException {
        Product product = Product.pickRandomProduct();
        forward(product, MAX_HOP_COUNT, new int[] {});
    }

    private record ReplyPath(int[] replyPath) { }
}
