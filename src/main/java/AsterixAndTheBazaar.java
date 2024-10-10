import peer.IPeer;

import java.net.*;
import java.util.*;

public class AsterixAndTheBazaar {

    public static int REGISTRY_ID = 1099;
    public static int NEIGHBORS_AMOUNT = 3;

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);  // Number of peers
        List<IPeer> peers = createNetwork(n);
    }

    public static List<IPeer> createNetwork(int n) throws SocketException {
        List<IPeer> peers = new ArrayList<>();

        Map<Integer, List<Integer>> nodes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            nodes.put(i, new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            nodes.get(i).add((i + 1) % n);
            nodes.get((i + 1) % n).add(i);
        }
        for (int nodeIndex = 0; nodeIndex < n; nodeIndex++) {
            for (int i = nodes.get(nodeIndex).size(); i < NEIGHBORS_AMOUNT; i++) {
                int neighborId = -1;

                for (int neighborIndex = 0; neighborIndex < n; neighborIndex++) {
                    if (neighborIndex != nodeIndex && // check that node is not the neighbor itself
                            nodes.get(neighborIndex).size() < 3 && // check that neighbor doesn't have more than two neighbors
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

        /**
        for (int i = 0; i < N; i++) {
            int port = 5000 + i;
            List<Integer> neighbors = new ArrayList<>();
            IPeer peer;
            if (rand.nextDouble() < 0.5) {
                peer = new Buyer(port, neighbors);
            } else {
                peer = new Seller(port, neighbors, rand.nextInt(10));
            }
            peers.add(peer);
        }

        // Ensure each peer has up to 3 neighbors
        for (IPeer peer : peers) {
            Collections.shuffle(peers);
            List<IPeer> potentialNeighbors = new ArrayList<>(peers);
            potentialNeighbors.remove(peer);  // A peer cannot be its own neighbor
            for (int i = 0; i < Math.min(3, potentialNeighbors.size()); i++) {
                peer.getNeighbors().add(potentialNeighbors.get(i).getPeerID());
            }
        }
        */

        return peers;
    }
}

