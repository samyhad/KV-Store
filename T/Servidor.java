package T;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class Servidor {

    private static Hashtable<InetSocketAddress, Boolean> hashTableServer = new Hashtable<>();
    private static Hashtable<Integer, Hashtable<String, Instant>> hashTableKV = new Hashtable<>();
    private static int port;
    private static String ipAddress;
    private static Boolean isLeader;

    /*public Servidor(int port, String ipAddress, Boolean isLeader) {
        this.port = port;
        this.ipAddress = ipAddress;
        this.isLeader = isLeader;
    }*/

    public Servidor() {
     
    }
    /*public int getPort() {
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
    }*/
    
    public static void main(String[] args) throws Exception{
        
        Servidor server = new Servidor();

        //ArrayList<Servidor> listaServidores = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("------------------ SERVER ------------------");
        System.out.println("Qual o IP desse servidor?");
        ipAddress = scanner.nextLine();
        System.out.println("Qual a porta desse servidor?");
        port = (scanner.nextInt());
        scanner.nextLine();
        System.out.println("Esse servidor será o servidor líder? (S|N)");
        String answerIsLeader = scanner.nextLine();
        if(answerIsLeader.equalsIgnoreCase("S")){
            isLeader = true;
            
        } else if (answerIsLeader.equalsIgnoreCase("N")){
            boolean rep = true;
            while(rep){
                try {
                    isLeader = false;
                    System.out.println("Qual o IP do líder?");
                    String leaderIp = scanner.nextLine();
                    System.out.println("Qual a porta do líder?");
                    int leaderPort = scanner.nextInt();
                    connectWithLeader(leaderIp, leaderPort);
                    //listaServidores.add(new Servidor(leaderPort, leaderIp, true));
                    hashTableServer.put(new InetSocketAddress(leaderIp, leaderPort), true);
                    rep = false;
                } catch (IOException e) {
                    System.err.println("Não foi possível se conectar a esse lider, tente outro servidor");
                    // Em caso de falha, o loop continuará e o usuário poderá digitar outro endereço.
                }
            }
            
        }

        InetSocketAddress endereco = new InetSocketAddress(ipAddress, port);
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(endereco);
        //listaServidores.add(new Servidor(server.port, server.ipAddress, server.getIsLeader()));
        //new InetSocketAddress(server.ipAddress, server.port), 
        hashTableServer.put(endereco, isLeader);
        
        
        ThreadServer th = new ThreadServer(serverSocket, server);
        th.start();
        
      
    }

    public static void connectWithLeader(String IpServer, int PortaServer) throws IOException, ClassNotFoundException{
        //Mensagem de conexão com o líder
        Mensagem msgConn = new Mensagem("CONN", port, ipAddress); //TYPE, K, V
        // Criando o socket - conexão TCP entre os dois servidores
        Socket socket_conn = new Socket(IpServer, PortaServer);
        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
        ObjectOutputStream out = new ObjectOutputStream(socket_conn.getOutputStream());
        // Serializa o objeto e envia para o servidor
        out.writeObject(msgConn);
        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
        ObjectInputStream in = new ObjectInputStream(socket_conn.getInputStream());
        // Recebe o objeto transmitido e realiza a deserialização
        Mensagem msg = (Mensagem) in.readObject();
        
        if(msg.getStatus().equals("CONN_OK")){
            System.out.println("Realizando conexão com o servidor líder");
        } else {
            System.out.println("Falha ao se conectar, esse fornecedor não é um líder");
        }
        in.close();
        out.close();
        socket_conn.close();
        
    }

    public static void connectWithOthersServer (String IP, int PORTA) throws IOException {
        Socket socket_conn = new Socket(IP, PORTA);
        socket_conn.close();
    }

    public static Hashtable<InetSocketAddress, Boolean> findSupportServer () throws IOException {
        Hashtable<InetSocketAddress, Boolean> serversReturned = new Hashtable<>();
        for (Entry<InetSocketAddress, Boolean> registro : hashTableServer.entrySet()) {
            if (registro.getValue() == false){
                serversReturned.put(registro.getKey(), registro.getValue());
            }
        }
        return serversReturned;
    }

    public static InetSocketAddress findLeader () throws IOException {
        for (Entry<InetSocketAddress, Boolean> registro : hashTableServer.entrySet()) {
            if (registro.getValue() == true){
                return registro.getKey();
            }
        }
        return null;
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
        public static Servidor server;
        /**
         * Construtor da classe
         * @param ss serverSocket utilizado pelo peer para realizar a conexão TCP 
 * @param server
         */
        public ThreadServer(ServerSocket ss, Servidor server) {
            serverSocket = ss;
            server = server;
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
                            if(msg.getType().equals("PUT")){
                                if(server.isLeader == true){
                                    Instant timesInstant = Instant.now();
                                    Mensagem msgReplication = new Mensagem("REPLICATION", msg.getKey(), msg.getValue(), timesInstant);
                                    
                                    Hashtable<InetSocketAddress, Boolean> serversToRep = findSupportServer();
                                    int t = serversToRep.size();
                                    int cont = 0;
                                    for (InetSocketAddress chave : serversToRep.keySet()) {
                                        Socket sRep = new Socket(chave.getHostString(), chave.getPort());    
                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream outServ = new ObjectOutputStream(sRep.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        outServ.writeObject(msgReplication);
                                        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
                                        ObjectInputStream inServ = new ObjectInputStream(sRep.getInputStream());
                                        // Recebe o objeto transmitido e realiza a deserialização
                                        Mensagem msgReturn = (Mensagem) inServ.readObject();
                                        if(msgReturn.getStatus().equals("REPLICATION_OK")){
                                            cont = cont + 1;
                                        }
                                        outServ.close();
                                        inServ.close();
                                        sRep.close();
                                    }
                                    if(cont == t){
                                        // Adiciona essa chave-valor ao Hashtable local do servidor
                                        addData(msg.getKey(), msg.getValue(), timesInstant);
                                        // Imprime no terminal do líder que a operação de PUT foi bem sucedida
                                        //TO DO
                                        System.out.println("Cliente ["
                                        +"IP"+"]:["
                                        +"PORTA"+"] PUT key:["
                                        + msg.getKey()+"] value:["
                                        +msg.getValue()+"]");

                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        out.writeObject(new Mensagem("PUT_OK", timesInstant));
                                        out.close();

                                    }
                                    in.close();
                                }
                                else{
                                    System.out.println("Encaminhando PUT key:["+msg.getKey()+"] value:["+msg.getValue()+"]");
                                    Mensagem msgSendPutToLeader = new Mensagem("PUT", msg.getKey(), msg.getValue());
                                    
                                    InetSocketAddress lider = findLeader();
                                    Socket s = new Socket(lider.getHostString(), lider.getPort());
                            
                                    // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                    ObjectOutputStream outServ = new ObjectOutputStream(s.getOutputStream());
                                    // Serializa o objeto e envia para o servidor
                                    outServ.writeObject(msgSendPutToLeader);
                                    // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
                                    ObjectInputStream inServ = new ObjectInputStream(s.getInputStream());
                                    // Recebe o objeto transmitido e realiza a deserialização
                                    Mensagem msgReturn = (Mensagem) inServ.readObject();

                                    if(msgReturn.getStatus().equals("PUT_OK")){
                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        out.writeObject(new Mensagem("PUT_OK", msgReturn.gettimestamp()));
                                        out.close();
                                    }
                                    // Fechando fluxos de entrada e saída de dados por TCP
                                    outServ.close();
                                    inServ.close();
                                    in.close();
                                }
                            } else if (msg.getType().equals("REPLICATION")) {
                                // Imprimindo detalhes da operação
                                System.out.println("REPLICATION key:["+msg.getKey()+"] value:["+msg.getValue()+"] ts:["+msg.gettimestamp()+"]");
                                // Adicionando KV ao hashtable local
                                addData(msg.getKey(), msg.getValue(), msg.gettimestamp());
                                // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                ObjectOutputStream outServ = new ObjectOutputStream(no.getOutputStream());
                                // Serializa o objeto e envia para o servidor
                                outServ.writeObject(new Mensagem("REPLICATION_OK"));
                                // Fechando fluxos de entrada e saída de dados por TCP
                                outServ.close();
                                in.close();
                               
                            } else if (msg.getType().equals("GET")) {

                                System.out.println("Cliente [IP]:[porta] GET key:[key] ts:[timestamp]. Meu ts é [timestamp_da_key], portanto devolvendo [valor ou erro]");

                            } else if (msg.getType().equals("CONN")){
                                if(server.isLeader == true){
                                    hashTableServer.put(new InetSocketAddress(msg.getValue(), msg.getKey()), false);
                                    System.out.println("Realizando conexão com o servidor requisitante");
                                    System.out.println(hashTableServer);
                                    // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                    ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                    // Serializa o objeto e envia para o servidor
                                    out.writeObject(new Mensagem("CONN_OK"));
                                    out.close();
                                } else {
                                    // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                    ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                    // Serializa o objeto e envia para o servidor
                                    out.writeObject(new Mensagem("CONN_NOK"));
                                    out.close();
                                }
                                
                            }
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
    
        public static void addData(int key, String value, Instant timestamp) {
            // Verifica se a tabela hash interna (interna ao primeiro nível) já existe
            if (!server.hashTableKV.containsKey(key)) {
                server.hashTableKV.put(key, new Hashtable<>());
            }

            // Adiciona o valor na tabela hash interna (interna ao primeiro nível)
            Hashtable<String, Instant> innerHashtable = server.hashTableKV.get(key);
            innerHashtable.put(value, timestamp);
        }

        public static Hashtable<String, Instant> retrieveValue (int key) {
            if (server.hashTableKV.containsKey(key)) {
                Hashtable<String, Instant> innerHashtable = server.hashTableKV.get(key);
                return innerHashtable;
            } else {
                return null;
            }
        }  
    
    }



    
}

