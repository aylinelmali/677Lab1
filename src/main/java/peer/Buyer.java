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

    private static final int MAX_RETRIES = 3;
    private static final int AVERAGE_AMOUNT = 100;

    private final List<ReplyPath> sellers;
    private Product product;
    private final PeerConfiguration peerConfiguration;
    private boolean acknowledged;
    private int retries;
    private int sequenceNumber;

    // for statistics:
    private volatile long sendingTime;
    private volatile List<Long> receivingTimeDeltas;

    public Buyer(int peerID, List<Integer> neighbors, Registry registry, PeerConfiguration peerConfiguration) throws RemoteException {
        super(peerID, neighbors, registry);
        product = Product.pickRandomProduct();
        sellers = new ArrayList<>();
        sequenceNumber = 0;
        this.peerConfiguration = peerConfiguration;
        acknowledged = true;
        retries = 0;
        sendingTime = 0;
        receivingTimeDeltas = new ArrayList<>();
    }

    @Override
    public void start() throws RemoteException {
        Logger.log("Peer " + peerID + " (Buyer)");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(1,3);
        int period = new Random().nextInt(2,3);

        executor.scheduleAtFixedRate(() -> {
            try {
                if (!sellers.isEmpty()) {
                    ReplyPath seller = sellers
                            .stream()
                            .skip((int) (sellers.size() * Math.random()))
                            .findFirst()
                            .orElseThrow();
                    sellers.clear();

                    buy(product, seller.replyPath);
                } else if (!acknowledged && retries < MAX_RETRIES) {
                    retryBuying();
                } else {
                    buyNewProduct();
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath, int sequenceNumber) throws RemoteException {
        sendingTime = System.currentTimeMillis();
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
        } else if (peerIndex == 0) { // this peer is the original buyer, add seller to the list.
            Logger.log(Messages.getReplyArrivedMessage(sellerID, product, peerID));
            if (product.equals(this.product)) {
                sellers.add(new ReplyPath(replyPath));
                acknowledged = false;
                receivingTimeDeltas.add(System.currentTimeMillis() - sendingTime);
                if (receivingTimeDeltas.size() >= AVERAGE_AMOUNT) {
                    Logger.logStats(Messages.getStatisticsMessage(peerID, receivingTimeDeltas));
                    receivingTimeDeltas.clear();
                }
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
            Logger.log(Messages.getBuyForwardMessage(path[0], product, peerID));
            peer.buy(product, path);
        } else {
            Logger.log(Messages.getForwardErrorMessage());
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
        } else if (peerIndex == 0) { // this peer is the original buyer, add seller to the list.
            Logger.log(Messages.getAckArrivedMessage(sellerID, product, peerID));
            acknowledged = true;
        }
    }

    /**
     * Picks a random new product and forwards a lookup message to each peer.
     */
    private void buyNewProduct() throws RemoteException {
        product = Product.pickRandomProduct();
        retries = 0;
        lookup(peerID, product, peerConfiguration.getMaxHopCount(), new int[] {}, sequenceNumber++);
    }

    private void retryBuying() throws RemoteException {
        retries++;
        lookup(peerID, product, peerConfiguration.getMaxHopCount(), new int[] {}, sequenceNumber++);
    }

    private record ReplyPath(int[] replyPath) { }
}
