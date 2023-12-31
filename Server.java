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
    public static ArrayList<Address> servers = new ArrayList<>();
    
    private int port;
    private String ipAddress;
    private Boolean isLeader;
    private static Address peer;
    
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
        System.out.println("------------------ SERVER ------------------");
        System.out.println("Qual o IP desse servidor?");
        server.setIpAddress(scanner.nextLine());
        System.out.println("Qual a porta desse servidor?");
        server.setPort(scanner.nextInt());
        
        scanner.nextLine();
        System.out.println("Esse servidor será o servidor líder? (S|N)");
        String answerIsLeader = scanner.nextLine();
        if(answerIsLeader == "S"){
            server.isLeader = true;
            
        } else if (answerIsLeader == "N"){
            boolean rep = true;
            while(rep){
                try {
                    server.isLeader =  false;
                    System.out.println("Qual o IP do líder?");
                    String leaderIp = scanner.nextLine();
                    System.out.println("Qual a porta do líder?");
                    int leaderPort = scanner.nextInt();
                    connectWithLeader(peer);
                    Address peerLeader = new Address(leaderIp, leaderPort, true);
                    servers.add(peerLeader);
                    rep = false;
                } catch (IOException e) {
                    System.err.println("Não foi possível se conectar a esse lider, tente outro servidor");
                    // Em caso de falha, o loop continuará e o usuário poderá digitar outro endereço.
                }
            }
            
        }

        InetSocketAddress endereco = new InetSocketAddress(server.getIpAddress(), server.getPort());
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(endereco);

        peer = new Address(server.ipAddress, server.port, server.getIsLeader());
        
        servers.add(peer);

        while(true) {
            //socket nó é o socket conectivo
            // socket nó terá um porta designada pelo SP - entre 1-24 e 65535
            //accept(): O método accept() escuta uma conexão e aceita se alguma for encontrada. 
            //O accept() bloqueia todo o restante até que uma conexão seja feita, 
            //ele fica em espera aguardando que alguém conecte. Quando alguma conexão é aceita ele 
            //retorna um objeto Socket, que veremos mais à frente.
            System.out.println("Esperando conexão com cliente");
            Socket no = serverSocket.accept(); //bloqueante - fica nesse ponto esperando ação
            System.out.println("Conexão aceita");
            
            //thread para atender novo nó
            ThreadAtendimento thread = new ThreadAtendimento(no);
            thread.start(); //executa a thread -> chamada ao método run()
        }
        
      
    }

    public static void connectWithLeader(Address peer) throws IOException{
        // Criando o socket - conexão TCP entre os dois servidores
        Socket socket_conn = new Socket(peer.IP, peer.PORTA);
        socket_conn.close();
    }

    public static void connectWithOthersServer (Address peer) throws IOException{
        Socket socket_conn = new Socket(peer.IP, peer.PORTA);
        socket_conn.close();
    }

    public static Address findLeader(){

        for (Address server : servers) {
            if (server.isLeader() == true) {
                return server;
            }
        }

        return null;
    }


    
}
