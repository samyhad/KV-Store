public class Peer {
    public String IP;
    public int PORTA;

    public Peer(String iP, int pORTA) {
        IP = iP;
        PORTA = pORTA;
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
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Peer other = (Peer) obj;
        if (IP == null) {
            if (other.IP != null)
                return false;
        } else if (!IP.equals(other.IP))
            return false;
        if (PORTA != other.PORTA)
            return false;
        return true;
    }
    
    
}
