package mk.ukim.finki.ds.lab4;

import java.io.IOException;
import java.net.Socket;

/**
 * Abstract Client class which connects to a server.
 */
public abstract class Client extends Thread {
    private static long counter = 0L;

    protected final long id;
    protected final String hostname;
    protected final int port;

    /**
     * Creates a Client instance that will connect to a given location.
     * @param hostname the hostname to connect to
     * @param port     the port to connect to
     */
    public Client(String hostname, int port) {
        this.id = counter++;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Communicates with the server via a Socket.
     * @param socket the Socket via which to communicate
     */
    public abstract void communicate(Socket socket);

    /**
     * Communicates with the server.
     * @see #communicate(Socket)
     */
    @Override
    public void run() {
        try (Socket socket = new Socket(hostname, port)) {
            communicate(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the String representation of the Client.
     * @return the String representation of the Client
     */
    @Override
    public String toString() {
        return String.format("Client [id: %d, hostname: %s, port: %d]",
                id, hostname, port);
    }
}
