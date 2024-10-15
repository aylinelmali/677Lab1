package peer;

import product.Product;
import utils.Logger;
import utils.Messages;
import utils.PeerConfiguration;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer {

    private final List<ReplyPath> sellers;
    private Product product;
    private final PeerConfiguration peerConfiguration;

    public Buyer(int peerID, List<Integer> neighbors, Registry registry, PeerConfiguration peerConfiguration) throws RemoteException {
        super(peerID, neighbors, registry);
        product = Product.pickRandomProduct();
        sellers = new ArrayList<>();
        this.peerConfiguration = peerConfiguration;
    }

    @Override
    public void start() throws RemoteException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(1,3);
        int period = new Random().nextInt(2,3);

        executor.scheduleAtFixedRate(() -> {
            try {
                if (sellers.isEmpty()) {
                    buyNewProduct();
                    return;
                }

                ReplyPath seller = sellers
                        .stream()
                        .skip((int) (sellers.size() * Math.random()))
                        .findFirst()
                        .orElseThrow();
                sellers.clear();

                buy(product, seller.replyPath);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {
        forward(product, hopCount, searchPath);
        Logger.log(Messages.getLookupForwardMessage(buyerID, product, peerID));
    }

    @Override
    public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {
        int peerIndex = getPeerIndex(replyPath);

        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path.
            IPeer peer = getNeighbors().get(replyPath[peerIndex - 1]);
            if (peer != null) {
                peer.reply(sellerID, product, replyPath);
                Logger.log(Messages.getReplyForwardMessage(sellerID, product, peerID));
            } else {
                Logger.log(Messages.getForwardErrorMessage());
            }
        } else if (peerIndex == 0) { // this peer is the original buyer, add seller to the list.
            Logger.log(Messages.getReplyArrivedMessage(sellerID, product, peerID));
            if (product.equals(this.product)) {
                sellers.add(new ReplyPath(replyPath));
            } else {
                Logger.log(Messages.getWrongReplyMessage(sellerID, product, peerID));
            }
        }
    }

    @Override
    public void buy(Product product, int[] path) throws RemoteException {
        int peerIndex = getPeerIndex(path);
        IPeer peer = getNeighbors().get(path[peerIndex + 1]);
        if (peer != null) { // forward message to the next peer.
            peer.buy(product, path);
            Logger.log(Messages.getBuyForwardMessage(path[0], product, peerID));
        } else {
            Logger.log(Messages.getForwardErrorMessage());
        }
    }

    /**
     * Picks a random new product and forwards a lookup message to each peer.
     */
    private void buyNewProduct() throws RemoteException {
        product = Product.pickRandomProduct();
        forward(product, peerConfiguration.getMaxHopCount(), new int[] {});
    }

    private record ReplyPath(int[] replyPath) { }
}
