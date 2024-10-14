package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IPeer extends Remote {

    int MAX_HOP_COUNT = 3;

    void start() throws RemoteException;
    void lookup(int buyerID, Product product, int hopCount, int[] searchPath) throws RemoteException;
    void reply(int sellerID, int[] replyPath) throws RemoteException;
    void buy(int[] path) throws RemoteException;
    int getPeerID() throws RemoteException;
    Map<Integer, IPeer> getNeighbors() throws RemoteException;
}
