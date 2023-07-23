import java.io.Serializable;

public class Mensagem implements Serializable{
    private String type;
    private int key;
    private String value;
    private long timestampMillis;
    
    public Mensagem(String type, int key, String value, long timestampMillis) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.timestampMillis = timestampMillis;
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
    public long getTimestampMillis() {
        return timestampMillis;
    }
    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + key;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + (int) (timestampMillis ^ (timestampMillis >>> 32));
        return result;
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
        if (timestampMillis != other.timestampMillis)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Mensagem [type=" + type 
        + ", key=" + key 
        + ", value=" + value 
        + ", timestampMillis=" + timestampMillis
        + "]";
    }
    
}
