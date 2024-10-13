package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IPeer extends Remote {

    int MAX_HOP_COUNT = 3;

    void start() throws RemoteException;
    void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException;
    void reply(int sellerID, int[] replyPath) throws RemoteException;
    void buy(int peerID,int[] path) throws RemoteException;
    int getPeerID() throws RemoteException;
    List<IPeer> getNeighbors() throws RemoteException;
}
