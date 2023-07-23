package T;

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
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private Hashtable<Integer, String> hashTableKV;
    
    private int port;
    private String ipAddress;
    private Boolean isLeader;

    public Servidor(int port, String ipAddress, Boolean isLeader) {
        this.port = port;
        this.ipAddress = ipAddress;
        this.isLeader = isLeader;
    }

    public Servidor() {
     
    }

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
        
        Servidor server = new Servidor();

        ArrayList<Servidor> listaServidores = new ArrayList<>();

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
                    connectWithLeader(leaderIp, leaderPort);
                    listaServidores.add(new Servidor(leaderPort, leaderIp, true));
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

        listaServidores.add(new Servidor(server.port, server.ipAddress, server.getIsLeader()));
        
        
        ThreadServer th = new ThreadServer(serverSocket);
        th.start();
        
      
    }

    public static void connectWithLeader(String IP, int PORTA) throws IOException{
        // Criando o socket - conexão TCP entre os dois servidores
        Socket socket_conn = new Socket(IP, PORTA);
        socket_conn.close();
    }

    public static void connectWithOthersServer (String IP, int PORTA) throws IOException{
        Socket socket_conn = new Socket(IP, PORTA);
        socket_conn.close();
    }

    /*public static Address findLeader(){

        for (Address server : servers) {
            if (server.isLeader() == true) {
                return server;
            }
        }

        return null;
    }*/
    public static class ThreadServer extends Thread{
        
        //Server socket utilizado nessa conexão TCP
        public static ServerSocket serverSocket;
        /**
         * Construtor da classe
         * @param ss serverSocket utilizado pelo peer para realizar a conexão TCP 
         */
        public ThreadServer(ServerSocket ss) {
            serverSocket = ss;
        }
        /**
         * Rodando a thread que irá escutar as solicitações bem como realizar as transferências.
         */
        public void run(){
            try{

                while(true) {
                    Socket no = serverSocket.accept(); // Espera por uma conexão

                    //thread para realizar trasferência
                    Thread th_accept = new Thread(() -> {
                        
                        try {
                            // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
                            ObjectInputStream in = new ObjectInputStream(no.getInputStream());
                            // Recebe o objeto transmitido e realiza a deserialização
                            Mensagem msg = (Mensagem) in.readObject();
                            // Imprime mensagem recebida
                            System.out.println("Mensagem recebida: " + msg);
                            in.close();
                        } catch (IOException | ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                    // inicializa a thread
                    th_accept.start();
                }
                
            }catch(Exception e){
                System.err.println(e);
            }
        }
    }



    
}

