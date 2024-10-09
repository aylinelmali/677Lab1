package peer;

import product.Product;
import utils.Logger;

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
        if(this.productType.equals(product) && itemStock > 0){
            reply(peerID, searchPath);
        }
        else{
            forward(buyerID, product, hopCount, searchPath);
        }
    }

    @Override
    public void reply(int sellerID, int[] replyPath) {
        int peerIndex = getPeerIndex(replyPath);
        if (peerIndex > 0) { // this peer should forward the message to the next peer in the path
            getNeighbors().get(peerIndex - 1).reply(sellerID, replyPath);
        }
    }

    @Override
    public void buy(int peerID, int[] path) {
        if (decrementStock()) {
            System.out.println("Transaction successful with buyer " + peerID + ". Remaining stock: " + itemStock);
            if(itemStock <= 0){
                Random rand = new Random();
                this.productType = getRandomProduct();
                this.itemStock = rand.nextInt(10);
            }
        } else {
            System.out.println("Out of stock! Cannot complete the transaction.");
        }
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
