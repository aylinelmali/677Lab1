package oldClasses;

import java.net.*;
import java.io.*;
import java.util.*;

public class Peer implements Runnable {
    protected int peerId;
    protected int port;
    protected List<Integer> neighbors;
    protected DatagramSocket socket;

    public Peer(int peerId, int port, List<Integer> neighbors) throws SocketException {
        this.peerId = peerId;
        this.port = port;
        this.neighbors = neighbors;
        this.socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                handleRequest(received, packet.getAddress(), packet.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void handleRequest(String data, InetAddress address, int port) {
        // To be overridden by subclasses
    }
}

class Buyer extends Peer {
    public Buyer(int peerId, int port, List<Integer> neighbors) throws SocketException {
        super(peerId, port, neighbors);
    }

    public void lookup(int buyerId, String productName, int hopCount, List<Integer> searchPath) throws IOException {
        searchPath.add(this.peerId);
        if (this instanceof Seller && ((Seller) this).getItemType().equals(productName)) {
            ((Seller) this).reply(buyerId, searchPath);
            return;
        }

        if (hopCount > 0) {
            for (int neighbor : neighbors) {
                String message = String.format("lookup|%d|%s|%d|%s", buyerId, productName, hopCount - 1, searchPath);
                sendMessage(neighbor, message);
            }
        }
    }

    public void buy(int sellerId, List<Integer> path) throws IOException {
        if (this.peerId == sellerId) {
            if (((Seller) this).decrementStock()) {
                System.out.println("Buyer " + this.peerId + " bought an item from Seller " + sellerId);
            } else {
                System.out.println("Seller " + sellerId + " has no stock left.");
            }
        } else {
            String message = "buy|" + this.peerId + "|" + path;
            sendMessage(sellerId, message);
        }
    }

    protected void sendMessage(int peerId, String message) throws IOException {
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),
                InetAddress.getByName("127.0.0.1"), 5000 + peerId);
        socket.send(packet);
    }
}

class Seller extends Buyer {
    private int itemStock;
    private String itemType;

    public Seller(int peerId, int port, List<Integer> neighbors, int stock) throws SocketException {
        super(peerId, port, neighbors);
        this.itemStock = stock;
        this.itemType = getRandomItem();
    }

    public String getItemType() {
        return itemType;
    }

    public boolean decrementStock() {
        if (itemStock > 0) {
            itemStock--;
            return true;
        }
        return false;
    }

    public void reply(int sellerId, List<Integer> replyPath) throws IOException {
        String message = "reply|" + sellerId + "|" + replyPath;
        if (replyPath.size() > 1) {
            int nextPeerId = replyPath.get(replyPath.size() - 2);
            replyPath.remove(replyPath.size() - 1);
            sendMessage(nextPeerId, message);
        } else {
            System.out.println("Seller " + sellerId + " reply sent directly to buyer");
        }
    }

    private String getRandomItem() {
        String[] items = {"fish", "salt", "boar"};
        return items[new Random().nextInt(items.length)];
    }
}

