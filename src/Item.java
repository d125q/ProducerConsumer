import java.io.Serializable;
import java.util.UUID;

/**
 * Class representing Items which are produced and consumed.
 */
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID uuid;

    public Item() {
        uuid = UUID.randomUUID();
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return uuid != null ? uuid.equals(item.uuid) : item.uuid == null;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Item [%s]", uuid);
    }
}
