package mk.ukim.finki.ds.lab4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * ServerQueue for Producers and Consumers.  Uses a BlockQueue internally and
 * notifies Producers as resources are needed, and Consumers as resources are
 * available.
 */
public class ServerQueue {
    private final int producerPort, consumerPort;
    private final BlockingQueue<Item> queue;

    /**
     * Creates a ServerQueue with a predefined capacity that will listen for
     * Producers and Consumers on specified ports.
     * @param capacity     the capacity of the queue
     * @param producerPort the port on which to listen for Producers
     * @param consumerPort the port on which to listen for Consumers
     * @see Producer
     * @see Consumer
     */
    private ServerQueue(int capacity, int producerPort, int consumerPort) {
        this.producerPort = producerPort;
        this.consumerPort = consumerPort;
        this.queue = new LinkedBlockingDeque<>(capacity);
    }

    public static void main(String[] args) {
        ServerQueue server = new ServerQueue(5, 50000, 50001);
        server.listen();
    }

    /**
     * Listens for Producers and Consumers in parallel, spawning new Threads
     * as they come.
     */
    private void listen() {
        Thread producerListener = new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket producerSocket = new ServerSocket(producerPort)) {
                    while (!Thread.currentThread().isInterrupted()) {
                        new ProducerHandler(producerSocket.accept()).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread consumerListener = new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket consumerSocket = new ServerSocket(consumerPort)) {
                    while (!Thread.currentThread().isInterrupted()) {
                        new ConsumerHandler(consumerSocket.accept()).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread[] threads = new Thread[]{producerListener, consumerListener};

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the String representation of the ServerQueue.
     * @return the String representation of the ServerQueue
     */
    public String toString() {
        return String.format("ServerQueue [producerPort: %d, consumerPort: %d]",
                producerPort, consumerPort);
    }

    /**
     * Abstract Handler class to handle Producers and Consumers.
     */
    private abstract class Handler extends Thread {
        protected final Socket socket;

        Handler(Socket socket) {
            super();
            this.socket = socket;
        }

        protected abstract void handle() throws IOException,
                InterruptedException;

        @Override
        public void run() {
            try {
                handle();
            } catch (InterruptedException | IOException e) {
                interrupt();
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Concrete class to handle Producers.
     */
    private class ProducerHandler extends Handler {
        ProducerHandler(Socket socket) {
            super(socket);
        }

        @Override
        public void handle() throws IOException, InterruptedException {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                while (!interrupted()) {
                    Item item = (Item) in.readObject();
                    queue.put(item); // This will block appropriately.
                    out.writeObject("ACK");
                    out.flush();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Concrete class to handle Consumers.
     */
    private class ConsumerHandler extends Handler {
        ConsumerHandler(Socket socket) {
            super(socket);
        }

        @Override
        public void handle() throws IOException, InterruptedException {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            while (!interrupted()) {
                Item item = queue.take(); // This will block appropriately.
                out.writeObject(item);
            }
        }
    }
}
