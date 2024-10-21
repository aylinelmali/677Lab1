import peer.Buyer;
import peer.IPeer;
import peer.Seller;
import utils.Logger;
import utils.PeerConfiguration;

import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class AsterixAndTheBazaar {

    public static int REGISTRY_ID = 1099;
    public static int NEIGHBORS_AMOUNT = 3;
    public static PeerConfiguration peerConfiguration;

    public static void main(String[] args) throws Exception {

        int n = Integer.parseInt(args[0]);  // Number of peers

        peerConfiguration = new PeerConfiguration();
        peerConfiguration.setMaxHopCount(n/Math.min(n,NEIGHBORS_AMOUNT));

        List<IPeer> peers = createNetwork(n);
        Logger.log("########## START INITIAL SETUP ##########");
        for (IPeer peer : peers) {
            peer.start();
        }
        Logger.log("########### END INITIAL SETUP ###########");
    }

    public static List<IPeer> createNetwork(int n) throws InterruptedException, RemoteException, NotBoundException {
        List<IPeer> peers = new ArrayList<>();

        Map<Integer, List<Integer>> nodes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            nodes.put(i, new ArrayList<>());
        }
        for (int i = 0; i < n && n > 2 || i == 0 && n == 2; i++) {
            nodes.get(i).add((i + 1) % n);
            nodes.get((i + 1) % n).add(i);
        }
        for (int nodeIndex = 0; nodeIndex < n; nodeIndex++) {
            for (int i = nodes.get(nodeIndex).size(); i < NEIGHBORS_AMOUNT; i++) {
                int neighborId = -1;

                for (int neighborIndex = 0; neighborIndex < n; neighborIndex++) {
                    if (neighborIndex != nodeIndex && // check that node is not the neighbor itself
                            nodes.get(neighborIndex).size() < NEIGHBORS_AMOUNT && // check that neighbor doesn't have more than two neighbors
                            !nodes.get(neighborIndex).contains(nodeIndex)) { // check that neighbor doesn't already have node as neighbor.
                        neighborId = neighborIndex;
                        break;
                    }
                }

                if (neighborId == -1) {
                    break;
                }

                nodes.get(nodeIndex).add(neighborId);
                nodes.get(neighborId).add(nodeIndex);
            }
        }

        Registry registry = LocateRegistry.createRegistry(REGISTRY_ID);

        for (int nodeIndex = 0; nodeIndex < n; nodeIndex++) {
            Thread t = getThread(nodeIndex, nodes.get(nodeIndex));
            t.start();
        }

        Thread.sleep(1000); // ensure that all peers are bound

        for (int nodeIndex = 0; nodeIndex < n; nodeIndex++) {
            peers.add((IPeer) registry.lookup("" + nodeIndex));
        }

        return peers;
    }

    private static Thread getThread(int nodeIndex, List<Integer> neighbors) {
        return new Thread(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_ID);

                IPeer peer = nodeIndex % 2 == 0 ? new Buyer(nodeIndex, neighbors, registry, peerConfiguration) : new Seller(nodeIndex, neighbors, registry);
                registry.rebind("" + peer.getPeerID(), peer);

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

