import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Producer client which connects to a ServerQueue and adds items to it.
 */
public class Producer extends Client {
    private static final boolean LOG = true;

    /**
     * Creates a Producer which will connect to a ServerQueue at a given
     * location.
     * @param hostname the hostname of the ServerQueue
     * @param port     the port of the ServerQueue
     */
    private Producer(String hostname, int port) {
        super(hostname, port);
    }

    public static void main(String[] args) {
        Producer[] producers = {
                new Producer("localhost", 50000),
                new Producer("localhost", 50000),
                new Producer("localhost", 50000),
                new Producer("localhost", 50000),
                new Producer("localhost", 50000)
        };

        for (Producer producer : producers)
            producer.start();

        for (Producer producer : producers) {
            try {
                producer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    /**
     * Communicates with the ServerQueue via a socket, adding resources as
     * they are needed.
     * @param socket the Socket via which to communicate
     * @see Client#communicate(Socket)
     */
    @Override
    public void communicate(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            while (!interrupted()) {
                Item item = new Item();
                out.writeObject(item);
                out.flush();
                String reply = (String) in.readObject();
                if (!reply.equalsIgnoreCase("ACK")) {
                    break;
                } else if (LOG) {
                    System.out.printf("%s put %s%n", this, item);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the String representation of the Producer.
     * @return the String representation of the Producer
     */
    @Override
    public String toString() {
        return String.format("Producer [id: %d, hostname: %s, port: %d]",
                id, hostname, port);
    }
}
