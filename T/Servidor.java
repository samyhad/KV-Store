package T;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class Servidor {

    private static Hashtable<InetSocketAddress, Boolean> hashTableServer = new Hashtable<>();
    private static Hashtable<Integer, Object[]> hashTableKV = new Hashtable<>();
    private static int port;
    private static String ipAddress;
    private static Boolean isLeader;

    public Servidor() {
     
    }    
    public static void main(String[] args) throws Exception{
        
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
                    // Verifica se é possível se conectar a esse líder
                    if (connectWithLeader(leaderIp, leaderPort)){
                        rep = false;
                        hashTableServer.put(new InetSocketAddress(leaderIp, leaderPort), true);
                    } else {
                        System.out.println("Não foi possível se conectar a esse lider, tente outro servidor");
                    }
                    
                } catch (IOException e) {
                    // Em caso de falha, o loop continuará e o usuário poderá digitar outro endereço.
                    System.err.println("Não foi possível se conectar a esse lider, tente outro servidor");
                }
            }
        }

        InetSocketAddress endereco = new InetSocketAddress(ipAddress, port);
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(endereco);
        hashTableServer.put(endereco, isLeader);
        
        ThreadServer th = new ThreadServer(serverSocket);
        th.start();
        
    }

    public static Boolean connectWithLeader(String IpServer, int PortaServer) throws IOException, ClassNotFoundException{
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
        
        in.close();
        out.close();
        socket_conn.close();

        if(!msg.getStatus().equals("CONN_OK")){
            return false;
        } else {
            return true;
        }
        
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

    public static class ThreadServer extends Thread{
        
        //Server socket utilizado nessa conexão TCP
        public static ServerSocket serverSocket;
        //public static Servidor server;
        /**
         * Construtor da classe
         * @param ss serverSocket utilizado pelo peer para realizar a conexão TCP 
         * @param server
         */
        public ThreadServer(ServerSocket ss) {
            serverSocket = ss;
            //server = server;
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
                                // Caso o servidor seja líder
                                if(isLeader == true){
                                    // Pega o timestamp do servidor
                                    Instant timesInstant = Instant.now();
                                    // Cria uma mensagem para replicar essa solicitação de PUT para os servidores de suporte
                                    Mensagem msgReplication = new Mensagem("REPLICATION", msg.getKey(), msg.getValue(), timesInstant);
                                    // Encontra os servidores de suporte
                                    Hashtable<InetSocketAddress, Boolean> serversToReplicate = findSupportServer();
                                    // Salva o número de servidores de suporte
                                    int t = serversToReplicate.size();
                                    // cria uma variável de contagem
                                    int cont = 0;
                                    // loop para enviar a mensagem de replicação para cada um dos servidores de suporte
                                    for (InetSocketAddress chave : serversToReplicate.keySet()) {
                                        // Cria socket com conexão entre o líder e o servidor de suporte
                                        Socket sRep = new Socket(chave.getHostString(), chave.getPort());    
                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream outServ = new ObjectOutputStream(sRep.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        outServ.writeObject(msgReplication);
                                        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
                                        ObjectInputStream inServ = new ObjectInputStream(sRep.getInputStream());
                                        // Recebe o objeto transmitido e realiza a deserialização
                                        Mensagem msgReturn = (Mensagem) inServ.readObject();
                                        // Verifica se o servidor de suporte conseguiu replicar essa KV
                                        if(msgReturn.getStatus().equals("REPLICATION_OK")){
                                            cont = cont + 1; // add +1 no nosso contador
                                        }
                                        // Fecha fluxos de entrada, saída e socket da comunicação entre servidor líder e de suporte
                                        outServ.close();
                                        inServ.close();
                                        sRep.close();
                                    }
                                    // Verifica se todos os servidores replicaram corretamente a KV
                                    if(cont == t){
                                        // Adiciona essa chave-valor ao Hashtable do líder
                                        addData(msg.getKey(), msg.getValue(), timesInstant);
                                        //TO DO
                                        System.out.println("Cliente ["
                                        + msg.getAddress().getHostString() +"]:["
                                        + msg.getAddress().getPort() +"] PUT key:["
                                        + msg.getKey()+"] value:["
                                        + msg.getValue()+"]");

                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        out.writeObject(new Mensagem("PUT_OK", timesInstant));
                                        out.close();

                                    } else {
                                        // Se não conseguirmos replicar o PUT para todos os servidores de suporte enviamos uma mensagem de erro
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        out.writeObject(new Mensagem("PUT_NOK"));
                                        out.close();

                                    }
                                    in.close();
                                }
                                else{
                                    System.out.println("Encaminhando PUT key:["+msg.getKey()+"] value:["+msg.getValue()+"]");
                                    Mensagem msgSendPutToLeader = new Mensagem("PUT", msg.getKey(), msg.getValue(), msg.getAddress());
                                    
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

                                Object[] retrivedObject = retrieveValue(msg.getKey());
                                String devolutiva;
                                Instant timestamp = null;
                                if(retrivedObject == null){
                                    //retornar null
                                    devolutiva = null;
                                    // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                    ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                    // Serializa o objeto e envia para o servidor
                                    out.writeObject(new Mensagem("TRY_OTHER_SERVER_OR_LATER", devolutiva, null));
                                    //fechando fluxo da saida de dados
                                    out.close();
                                } else {
                                    String value = (String) retrivedObject[0];
                                    timestamp = (Instant) retrivedObject[1];
                                    if (msg.gettimestamp() == null
                                        || timestamp.equals(msg.gettimestamp())
                                        || timestamp.isAfter(msg.gettimestamp())){
                                        // retorna o valor que encontramos
                                        devolutiva = value;
                                        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        out.writeObject(new Mensagem("GET_OK", devolutiva, timestamp));
                                        //fechando fluxo da saida de dados
                                        out.close();
                                    } else {
                                        // retorna erro
                                        devolutiva = "TRY_OTHER_SERVER_OR_LATER";
                                        ObjectOutputStream out = new ObjectOutputStream(no.getOutputStream());
                                        // Serializa o objeto e envia para o servidor
                                        out.writeObject(new Mensagem(devolutiva, null, null));
                                        //fechando fluxo da saida de dados
                                        out.close();
                                    }
                                }                                

                                System.out.println("Cliente ["
                                + msg.getAddress().getHostString() +"]:["
                                + msg.getAddress().getPort() +"] GET key:["
                                + msg.getKey()+"] ts:["
                                + msg.gettimestamp() +"]. Meu ts é ["
                                + timestamp +"], portanto devolvendo ["
                                + devolutiva +"]");

                            } else if (msg.getType().equals("CONN")) {
                                if(isLeader == true){
                                    hashTableServer.put(new InetSocketAddress(msg.getValue(), msg.getKey()), false);
                                    //System.out.println("Realizando conexão com o servidor requisitante");
                                    //System.out.println(hashTableServer);
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
            // Adiciona o valor na tabela hash local
            hashTableKV.put(key, new Object[] {value, timestamp});
        }

        public static Object[] retrieveValue (int key) {
            // Recupera o valor associado a essa chave bem como o timestamp

            // Verifica se temos essa chave dentro da nossa hashtable
            if (hashTableKV.containsKey(key)) {
                Object[] retrivedObject = hashTableKV.get(key);
                // Retorna um objeto com valor e timestamp associado a essa key
                String value = (String) retrivedObject[0];
                Instant timestamp = (Instant) retrivedObject[1];
                return new Object[] {value, timestamp};
            } else {
                return null;
            }
        }  
    
    }



    
}

