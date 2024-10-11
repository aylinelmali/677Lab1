import peer.Buyer;
import peer.IPeer;
import peer.Seller;

import java.net.*;
import java.util.*;

public class NetworkCreator {

    public static int REGISTRY_ID = 1099;

    public static void main(String[] args) throws Exception {
        int N = Integer.parseInt(args[0]);  // Number of peers
        List<IPeer> peers = createNetwork(N);

        for (IPeer peer : peers) {
            new Thread(peer).start();  // Start listening for each peer
        }
    }

    public static List<IPeer> createNetwork(int N) throws SocketException {
        List<IPeer> peers = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < N; i++) {
            int port = 5000 + i;
            List<Integer> neighbors = new ArrayList<>();
            IPeer peer;
            if (rand.nextDouble() < 0.5) {
                peer = new Buyer(port, neighbors);
            } else {
                peer = new Seller(port, neighbors, 5 + rand.nextInt(10));
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

        return peers;
    }
}

