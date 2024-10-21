package peer;

import product.Product;
import utils.Logger;
import utils.Messages;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public abstract class APeer extends UnicastRemoteObject implements IPeer {

    protected int peerID;
    protected List<Integer> neighbors;
    protected Map<Integer, IPeer> neighborPeers;
    private final Registry registry;

    public APeer(int peerID, List<Integer> neighbors, Registry registry) throws RemoteException {
        super();
        this.peerID = peerID;
        this.neighbors = neighbors;

        this.registry = registry;
    }

    @Override
    public int getPeerID() {
        return peerID;
    }

    @Override
    public Map<Integer, IPeer> getNeighbors() throws RemoteException {
        if (neighborPeers == null) {
            neighborPeers = new HashMap<>();
            neighbors.forEach(id -> {
                try {
                    IPeer peer = (IPeer) registry.lookup("" + id);
                    neighborPeers.put(id, peer);
                } catch (Exception e) {
                    neighborPeers.clear();
                    throw new RuntimeException(e);
                }
            });
        }
        return neighborPeers;
    }

    protected void forward(int buyerID, Product product, int hopCount, int[] searchPath, int sequenceNumber) throws RemoteException {
        if (hopCount <= 0) {
            Logger.log(Messages.getLookupDropped(buyerID, product, peerID));
            return;
        }

        // check if peer already forwarded this message. If yes, then drop the message.
        for (int value : searchPath) {
            if (peerID == value) {
                Logger.log(Messages.getLookupDropped(buyerID, product, peerID));
                return;
            }
        }

        int[] newSearchPath = getNewSearchPath(searchPath);

        Logger.log(Messages.getLookupForwardMessage(buyerID, product, peerID));

        for (IPeer neighbor : new ArrayList<>(getNeighbors().values())) {
            // check if peer already forwarded this message. If yes, then drop the message.
            for (int value : searchPath) {
                if (neighbor.getPeerID() == value) {
                    return;
                }
            }
            neighbor.lookup(buyerID, product, hopCount - 1, newSearchPath, sequenceNumber);
        }
    }

    protected int getPeerIndex(int[] replyPath) {
        int peerIndex = -1;
        for (int i = 0; i < replyPath.length; i++) {
            if (replyPath[i] == peerID) {
                peerIndex = i;
            }
        }
        return peerIndex;
    }

    protected int[] getNewSearchPath(int[] searchPath) {
        int[] newSearchPath = new int[searchPath.length + 1];
        System.arraycopy(searchPath, 0, newSearchPath, 0, searchPath.length);
        newSearchPath[searchPath.length] = peerID;
        return newSearchPath;
    }

}
