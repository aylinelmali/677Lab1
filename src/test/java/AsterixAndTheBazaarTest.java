import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import peer.Buyer;
import peer.IPeer;
import peer.Seller;
import product.Product;
import utils.PeerConfiguration;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AsterixAndTheBazaarTest {

    static Registry registry;
    static PeerConfiguration config;

    @BeforeAll
    public static void setup() throws RemoteException {
        registry = LocateRegistry.createRegistry(1009);
        config = new PeerConfiguration();
        config.setMaxHopCount(Integer.MAX_VALUE);
    }

    @BeforeEach
    public void before() throws RemoteException, NotBoundException {
        for (String binding : registry.list()) {
            registry.unbind(binding);
        }
    }

    @Test
    public void singleBranchLookupTest() throws RemoteException, AlreadyBoundException {
        IPeer peer0 = new Buyer(0, List.of(1), registry, config);
        registry.rebind("" + peer0.getPeerID(), peer0);
        IPeer peer1 = new Seller(1, List.of(0,2), registry, Product.BOARS);
        registry.rebind("" + peer1.getPeerID(), peer1);
        IPeer peer2 = new Buyer(2, List.of(1,3), registry, config);
        registry.rebind("" + peer2.getPeerID(), peer2);

        final int[] testBuyerID = {Integer.MAX_VALUE};
        final Product[] testProduct = {null};
        final int[] testHopCount = {Integer.MAX_VALUE};
        final int[][] testSearchPath = {new int[0]};

        IPeer peer3 = new IPeer() {
            @Override
            public void start() throws RemoteException {}

            @Override
            public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {
                testBuyerID[0] = buyerID;
                testProduct[0] = product;
                testHopCount[0] = hopCount;
                testSearchPath[0] = searchPath;
            }

            @Override
            public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {}

            @Override
            public void buy(Product product, int[] path) throws RemoteException {}

            @Override
            public int getPeerID() throws RemoteException {
                return 3;
            }

            @Override
            public Map<Integer, IPeer> getNeighbors() throws RemoteException {
                return Map.of();
            }
        };
        registry.rebind("" + peer3.getPeerID(), peer3);
        peer0.lookup(0, Product.FISH, Integer.MAX_VALUE, new int[] {});

        Assertions.assertEquals(0, testBuyerID[0]);
        Assertions.assertEquals(Product.FISH, testProduct[0]);
        Assertions.assertEquals(Integer.MAX_VALUE-3, testHopCount[0]);
        Assertions.assertArrayEquals(new int[] {0,1,2}, testSearchPath[0]);
    }

    @Test
    public void singleBranchReplyTest() throws RemoteException {
        IPeer peer0 = new Seller(0, List.of(1), registry);
        registry.rebind("" + peer0.getPeerID(), peer0);
        IPeer peer1 = new Buyer(1, List.of(0,2), registry, config);
        registry.rebind("" + peer1.getPeerID(), peer1);
        IPeer peer2 = new Seller(2, List.of(1,3), registry, Product.BOARS);
        registry.rebind("" + peer2.getPeerID(), peer2);

        final int[] testSellerID = {Integer.MAX_VALUE};
        final Product[] testProduct = {null};
        final int[][] testReplyPath = {null};

        IPeer peer3 = new IPeer() {
            @Override
            public void start() throws RemoteException {}

            @Override
            public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {}

            @Override
            public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {
                testSellerID[0] = sellerID;
                testProduct[0] = product;
                testReplyPath[0] = replyPath;
            }

            @Override
            public void buy(Product product, int[] path) throws RemoteException {}

            @Override
            public int getPeerID() throws RemoteException {
                return 3;
            }

            @Override
            public Map<Integer, IPeer> getNeighbors() throws RemoteException {
                return Map.of();
            }
        };
        registry.rebind("" + peer3.getPeerID(), peer3);
        peer0.reply(0, Product.BOARS, new int[] {3, 2, 1, 0});

        Assertions.assertEquals(0, testSellerID[0]);
        Assertions.assertEquals(Product.BOARS, testProduct[0]);
        Assertions.assertArrayEquals(new int[] {3, 2, 1, 0}, testReplyPath[0]);
    }

    @Test
    public void singleBranchBuyTest() throws RemoteException, AlreadyBoundException {
        IPeer peer0 = new Buyer(0, List.of(1), registry, config);
        registry.rebind("" + peer0.getPeerID(), peer0);
        IPeer peer1 = new Seller(1, List.of(0,2), registry, Product.BOARS);
        registry.rebind("" + peer1.getPeerID(), peer1);
        IPeer peer2 = new Buyer(2, List.of(1,3), registry, config);
        registry.rebind("" + peer2.getPeerID(), peer2);

        final int[][] testSearchPath = {new int[0]};
        final Product[] testProduct = {null};

        IPeer peer3 = new IPeer() {
            @Override
            public void start() throws RemoteException {}

            @Override
            public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {}

            @Override
            public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {}

            @Override
            public void buy(Product product, int[] path) throws RemoteException {
                testProduct[0] = product;
                testSearchPath[0] = path;
            }

            @Override
            public int getPeerID() throws RemoteException {
                return 3;
            }

            @Override
            public Map<Integer, IPeer> getNeighbors() throws RemoteException {
                return Map.of();
            }
        };
        registry.rebind("" + peer3.getPeerID(), peer3);
        peer0.buy(Product.BOARS, new int[] {0, 1, 2, 3});

        Assertions.assertEquals(Product.BOARS, testProduct[0]);
        Assertions.assertArrayEquals(new int[] {0, 1, 2, 3}, testSearchPath[0]);
    }

    @Test
    public void multiBranchLookupReplyTest() throws RemoteException {

        List<Integer> testSellerID = new ArrayList<>();
        List<Product> testProduct = new ArrayList<>();
        List<List<Integer>> testReplyPath = new ArrayList<>();

        IPeer peer0 = new IPeer() {
            @Override
            public void start() throws RemoteException {}

            @Override
            public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException {}

            @Override
            public void reply(int sellerID, Product product, int[] replyPath) throws RemoteException {
                testSellerID.add(sellerID);
                testProduct.add(product);
                testReplyPath.add(Arrays.stream(replyPath).boxed().toList());
            }

            @Override
            public void buy(Product product, int[] path) throws RemoteException {}

            @Override
            public int getPeerID() throws RemoteException {
                return 0;
            }

            @Override
            public Map<Integer, IPeer> getNeighbors() throws RemoteException {
                return Map.of();
            }
        };
        registry.rebind("" + peer0.getPeerID(), peer0);
        IPeer peer1 = new Seller(1, List.of(0,2), registry, Product.BOARS);
        registry.rebind("" + peer1.getPeerID(), peer1);
        IPeer peer2 = new Buyer(2, List.of(1,3,4), registry, config);
        registry.rebind("" + peer2.getPeerID(), peer2);
        IPeer peer3 = new Seller(3, List.of(2), registry, Product.FISH);
        registry.rebind("" + peer3.getPeerID(), peer3);
        IPeer peer4 = new Seller(4, List.of(2), registry, Product.FISH);
        registry.rebind("" + peer4.getPeerID(), peer4);

        peer1.lookup(0, Product.FISH, Integer.MAX_VALUE, new int[] { 0 });

        Assertions.assertEquals(2, testSellerID.size());
        Assertions.assertEquals(2, testProduct.size());
        Assertions.assertEquals(2, testReplyPath.size());

        Assertions.assertTrue(testSellerID.contains(3));
        Assertions.assertTrue(testSellerID.contains(4));
        Assertions.assertEquals(Product.FISH, testProduct.get(0));
        Assertions.assertEquals(Product.FISH, testProduct.get(1));
        Assertions.assertTrue(testReplyPath.contains(List.of(0, 1, 2, 3)));
        Assertions.assertTrue(testReplyPath.contains(List.of(0, 1, 2, 4)));
    }
}