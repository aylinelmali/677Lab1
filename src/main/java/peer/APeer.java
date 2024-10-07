package peer;

import product.Product;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public abstract class APeer extends UnicastRemoteObject implements IPeer {
    protected int peerID;
    protected List<Integer> neighbors;

    public APeer(int peerID, List<Integer> neighbors) throws RemoteException {
        super();
        this.peerID = peerID;
        this.neighbors = neighbors;
    }

    @Override
    public int getPeerID() {
        return peerID;
    }

    protected void forward(int buyerID, Product product, int hopCount, int[] searchPath) {
        if (hopCount >= MAX_HOP_COUNT) {
            return;
        }

        // check if buyerID already forwarded this message. If yes, then drop the message.
        for (int value : searchPath) {
            if (buyerID == value) {
                return;
            }
        }

        int[] newSearchPath = new int[searchPath.length + 1];
        System.arraycopy(searchPath, 0, newSearchPath, 0, searchPath.length);
        newSearchPath[searchPath.length] = buyerID;

        getNeighbors().forEach(neighbor -> neighbor.lookup(buyerID, product, hopCount, newSearchPath));
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
}
