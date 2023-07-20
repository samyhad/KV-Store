import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private Hashtable<Integer, String> hashTableKV;
    public static ArrayList<Peer> servers = new ArrayList<>();
    
    private int port;
    private String ipAddress;
    private Boolean isLeader;
    private static Peer peer;
    
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public Boolean getIsLeader() {
        return isLeader;
    }
    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }
    



    public static void main(String[] args) throws Exception{
        
        Server server = new Server();

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("Qual o IP desse servidor?");
        server.setIpAddress(scanner.nextLine());
        System.out.println("Qual a porta desse servidor?");
        server.setPort(scanner.nextInt());
        
        InetSocketAddress endereco = new InetSocketAddress(server.getIpAddress(), server.getPort());
        
        

        System.out.println("Esse servidor será o servidor líder? (S|N)");
        if(scanner.nextLine() == "S"){
            server.isLeader = true;
        } else if (scanner.nextLine() == "N"){
            server.isLeader =  false;
            System.out.println("Qual o IP do líder?");
            String leaderIp = scanner.nextLine();
            System.out.println("Qual a porta do líder?");
            int leaderPort = scanner.nextInt();
            Peer peerLeader = new Peer( InetAddress.getByName(leaderIp), leaderPort, true);
            servers.add(peerLeader);
        }

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(endereco);

        peer = new Peer(serverSocket.getInetAddress(), server.port, server.getIsLeader());
        
        servers.add(peer);


        
      
    }

    public static void connectWithLeader(Peer peer) throws IOException{
        // Criando o socket - conexão TCP entre os dois servidores
        Socket socket_conn = new Socket(peer.IP, peer.PORTA);
        socket_conn.close();
    }

    public static void connectWithOthersServer (Peer peer) throws IOException{
        Socket socket_conn = new Socket(peer.IP, peer.PORTA);
        socket_conn.close();
    }
}
