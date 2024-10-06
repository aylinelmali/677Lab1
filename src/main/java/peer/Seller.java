package peer;

import product.Product;

import java.util.List;
import java.util.Random;

public class Seller extends APeer {

    private int itemStock;
    private Product productType;

    public Seller(int peerID, List<Integer> neighbors, int stock) {
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
    public void lookup(int buyerID, Product product, int hopCount, List<Integer> searchPath) {

    }

    @Override
    public void reply(int sellerID, List<Integer> replyPath) {

    }

    @Override
    public void buy(int peerID, List<Integer> path) {

    }

    @Override
    public void run() {

    }

    public Product getProductType() {
        return productType;
    }
}
