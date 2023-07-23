
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class Address implements Serializable {
    public String IP;
    public int PORTA;
    public boolean isLeader;

    
    public Address(String iP, int pORTA, boolean isLeader) {
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

    public String getIP() {
        return IP;
    }

    public void setIP(String iP) {
        IP = iP;
    }

    public int getPORTA() {
        return PORTA;
    }

    public void setPORTA(int pORTA) {
        PORTA = pORTA;
    }
}
    