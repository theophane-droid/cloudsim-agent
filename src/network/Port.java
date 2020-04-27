package network;

/**
 * Represent an agentSwitch port
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
