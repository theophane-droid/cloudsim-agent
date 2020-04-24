package network;

/**
 * A class wich represent a network packet. This packet can be adressed to a AgentHost or to an AgentSwitch.
 * This class is not designed to be interpret by VM or Cloudlet.
 * @author Théophane Dumas
 */
public class RawPacket {
    private int idSrc;
    private int idDest;
    private Class classSrc;;
    private Class classDest;
    private Object content;
    // * usefull for test
    private Object recievedBy;

    public RawPacket(int idSrc, int idDest, Class classSrc, Class classDest, Object content) {
        this.idSrc = idSrc;
        this.idDest = idDest;
        this.classSrc = classSrc;
        this.classDest = classDest;
        this.content = content;
    }

    public int getIdSrc() {
        return idSrc;
    }

    public int getIdDest() {
        return idDest;
    }

    public Class getClassSrc() {
        return classSrc;
    }

    public Class getClassDest() {
        return classDest;
    }

    public Object getContent() {
        return content;
    }

    public void setRecievedBy(Object recievedBy) {
        this.recievedBy = recievedBy;
    }

    public Object getRecievedBy() {
        return recievedBy;
    }
}
