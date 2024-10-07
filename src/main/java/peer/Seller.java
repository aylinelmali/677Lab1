package peer;

import product.Product;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Seller extends APeer {

    private int itemStock;
    private Product productType;

    public Seller(int peerID, List<Integer> neighbors, int stock) throws RemoteException {
        super(peerID, neighbors);
        this.itemStock = stock;
        this.productType = getRandomProduct();
    }

    public boolean decrementStock() {
        if (itemStock > 0) {
            itemStock--;
            return true;
        }
        return false;
    }

    private Product getRandomProduct() {
        Product[] products = Product.values();
        return products[new Random().nextInt(products.length)];
    }

    @Override
    public void lookup(int buyerID, Product product, int hopCount, int[] searchPath) {

    }

    @Override
    public void reply(int sellerID, int[] replyPath) {

    }

    @Override
    public void buy(int peerID, int[] path) {

    }

    @Override
    public List<IPeer> getNeighbors() {
        return List.of();
    }

    public Product getProductType() {
        return productType;
    }

    public static void main(String[] args) {

    }
}
