
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class Peer implements Serializable {
    public InetAddress IP;
    public int PORTA;
    public boolean isLeader;

    
    public Peer(InetAddress iP, int pORTA, boolean isLeader) {
        IP = iP;
        PORTA = pORTA;
        isLeader = isLeader;

    }
    
    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress iP) {
        IP = iP;
    }

    public int getPORTA() {
        return PORTA;
    }

    public void setPORTA(int pORTA) {
        PORTA = pORTA;
    }
}
    