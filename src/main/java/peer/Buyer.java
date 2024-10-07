package peer;

import product.Product;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer {

    public static void main(String[] args) throws RemoteException {

        int id = Integer.parseInt(args[1]);
        String[] neighbourStrings = args[2].split(",");
        List<Integer> neighbours = new ArrayList<>();
        for (String neighbourString : neighbourStrings) {
            neighbours.add(Integer.parseInt(neighbourString));
        }

        Registry registry = LocateRegistry.getRegistry("localhost", 1009);

        IPeer peer = new Buyer(id, neighbours, registry);

        registry.rebind("" + peer.getPeerID(), peer);
    }

    private List<SellerIdReplyPathPair> sellers;
    private Registry registry;
    private List<IPeer> neighborPeers;

    public Buyer(int peerID, List<Integer> neighbors, Registry registry) throws RemoteException {
        super(peerID, neighbors);

        this.registry = registry;
        sellers = new ArrayList<>();

        buyNewProduct();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (sellers.isEmpty()) {
                return;
            }

            SellerIdReplyPathPair seller = sellers
                    .stream()
                    .skip((int) (sellers.size() * Math.random()))
                    .findFirst()
                    .orElseThrow();
            sellers.clear();

            buy(seller.sellerId, seller.replyPath);
            try {
                Thread.sleep(new Random().nextInt(1,3) * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            buyNewProduct(); // restart the cycle

        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) {
        forward(buyerID, product, hopCount, searchPath);
    }

    @Override
    public void reply(int sellerID, int[] replyPath) {
        int peerIndex = getPeerIndex(replyPath);

        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path
            getNeighbors().get(peerIndex - 1).reply(sellerID, replyPath);
        } else if (peerIndex == 0) { // this peer is the original buyer
            sellers.add(new SellerIdReplyPathPair(sellerID, replyPath));
        }
    }

    @Override
    public void buy(int peerID, int[] path) {
        int peerIndex = getPeerIndex(path);
        getNeighbors().get(peerIndex + 1).buy(peerID, path);
    }

    @Override
    public List<IPeer> getNeighbors() {
        if (neighborPeers == null) {
            neighbors.forEach(id -> {
                try {
                    IPeer peer = (IPeer) registry.lookup("" + id);
                    neighborPeers.add(peer);
                } catch (RemoteException | NotBoundException e) {
                    neighborPeers.clear();
                    throw new RuntimeException(e);
                }
            });
        }
        return neighborPeers;
    }

    private void buyNewProduct() {
        Product product = Product.pickRandomProduct();
        forward(peerID, product, 0, new int[] { peerID });
    }

    private static class SellerIdReplyPathPair {
        private int sellerId;
        private int[] replyPath;
        private SellerIdReplyPathPair(int sellerID, int[] replyPath) {
            this.sellerId = sellerID;
            this.replyPath = replyPath;
        }
    }
}
