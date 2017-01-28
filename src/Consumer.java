import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Consumer client which connects to a ServerQueue and consumes item from it.
 */
public class Consumer extends Client {
    private static final boolean LOG = true;

    /**
     * Creates a Consumer which will connect to a ServerQueue at a given
     * location.
     * @param hostname the hostname of the ServerQueue
     * @param port     the port of the ServerQueue
     */
    private Consumer(String hostname, int port) {
        super(hostname, port);
    }

    public static void main(String[] args) {
        Consumer[] consumers = {
                new Consumer("localhost", 50001),
                new Consumer("localhost", 50001),
                new Consumer("localhost", 50001),
                new Consumer("localhost", 50001)
        };

        for (Consumer consumer : consumers)
            consumer.start();

        for (Consumer consumer : consumers) {
            try {
                consumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    /**
     * Communicates with the ServerQueue via a socket, consuming resources as
     * they come.
     * @param socket the Socket via which to communicate
     * @see Client#communicate(Socket)
     */
    @Override
    public void communicate(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (!interrupted()) {
                Item item = (Item) in.readObject();
                if (LOG) {
                    System.out.printf("%s consumed %s%n", this, item);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the String representation of the Consumer.
     * @return the String representation of the Consumer
     */
    @Override
    public String toString() {
        return String.format("Consumer [id: %d, hostname: %s, port: %d]",
                id, hostname, port);
    }
}
