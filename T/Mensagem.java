package T;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.time.Instant;

public class Mensagem implements Serializable{
    private String type;
    private int key;
    private String value;
    private Instant timestamp;
    private String status;
    private InetSocketAddress address;
    
    //Construtor para REPLICATION
    public Mensagem(String type, int key, String value, Instant timestamp) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    //Construtor para PUT
    public Mensagem(String type, int key, String value, InetSocketAddress address) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.address = address;
    }

    //Construtor para o retorno do PUT (servidor)
    public Mensagem(String status, Instant timestamp){
        this.status = status;
        this.timestamp = timestamp;
    }

    //Construtor para o GET (cliente)
    public Mensagem(String type, int key, Instant timestamp, InetSocketAddress address) {
        this.type = type;
        this.key = key;
        this.timestamp = timestamp;
        this.address = address;
    }

    //Construtor para o retorno do GET (servidor)
    public Mensagem(String status, String value, Instant timestamp) {
        this.status = status;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    //Construtor para o REPLICATE_OK, CONN_OK, CONN_NOK
    public Mensagem(String status) {
        this.status = status;
    }

    //Construtor para o CONN
    public Mensagem(String type, int port, String ipAddress) {
        this.type = type;
        this.key = port;
        this.value = ipAddress;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Instant gettimestamp() {
        return timestamp;
    }
    public void settimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
        
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Mensagem other = (Mensagem) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (key != other.key)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Mensagem [type=" + type 
        + ", key=" + key 
        + ", value=" + value 
        + ", timestamp=" + timestamp
        + "]";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
