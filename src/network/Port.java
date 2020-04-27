package network;

/**
 * Represent an agentSwitch port, can have 2 states : off and on
 *
 * @author Théophane Dumas
 */
public class Port {
    private boolean isOpen;
    private Object reliedObject;

    public boolean isOpen() {
        return isOpen;
    }

    public Object getReliedObject() {
        return reliedObject;
    }
}
