package network;

/**
 * Represent an agentSwitch port, can have 2 states : off and on
 *
 * @author Théophane Dumas
 */
public class Port {
    private boolean isOpen;
    private final Object reliedObject;

    public Port(boolean isOpen, Object reliedObject) {
        this.isOpen = isOpen;
        this.reliedObject = reliedObject;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public Object getReliedObject() {
        return reliedObject;
    }
}
