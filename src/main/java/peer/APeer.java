package peer;

import java.util.List;

public abstract class APeer implements IPeer {
    protected int peerID;
    protected List<Integer> neighbors;

    public APeer(int peerID, List<Integer> neighbors) {
        this.peerID = peerID;
        this.neighbors = neighbors;
    }

    @Override
    public int getPeerID() {
        return peerID;
    }

    @Override
    public List<Integer> getNeighbors() {
        return neighbors;
    }
}
