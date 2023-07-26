package T;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.server.ServerNotActiveException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class Cliente {
    public static Hashtable<Integer, Hashtable<String, Instant>> hashTableKV = new Hashtable<>();
    public static ArrayList<InetSocketAddress> servers = new ArrayList<>();
    public static Scanner scanner;
    public static String IP;
    public static int PORTA;

    public static void main(String[] args) throws IOException {
        
        // inicializando a variável de scanner para realizarmos a leitura de teclado
        scanner = new Scanner(System.in);

        //chamando o método do Menu interativo
        try {
            menu();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    }

    public static void menu() throws IOException, ClassNotFoundException{
        
        System.out.println("------------------ PEER ------------------");
        //criando uma variável que controlará o fim do loop do nosso menu interativo
        Boolean condicao = true;
        //criando uma variável para registrar o endereço do peer
        String address_str = null;
        //criando uma variável para registrar a porta do peer
        int porta = -1;
        int input = -1;
        
        //LOOP do menu interativo
        while(condicao == true){
            //imprimindo opções no terminal para o usuário poder escolher qual opção ele quer
            System.out.println("Qual a requisi\u00E7\u00E3o desejada? (apenas n\u00FAmero)");
            System.out.println("[0]: INIT");
            System.out.println("[1]: PUT");
            System.out.println("[2]: GET");
            System.out.println("[3]: LEAVE");
            //lê entrada do usuário
            try{
                input = scanner.nextInt();
            }catch (InputMismatchException exception){
                System.out.println("Digite apenas valores inteiros por favor");
            }

            //se o usuário digitou 0 [INIT]
            if(input == 0){
                try {
                    //verifica se o JOIn já foi realizado
                    if(address_str == null && porta == -1){
                        scanner.nextLine();
                        System.out.println("Qual o IP desse peer?");
                        address_str = scanner.nextLine();
                        System.out.println("Qual a porta desse peer?");
                        porta = scanner.nextInt();

                        
                        //cria uma variável InetSocketAddress para registrar endereço e porta do peer
                        InetSocketAddress endereco = new InetSocketAddress(address_str, porta);
                        //criando serverSocket que servirá para realizarmos a conexão TCP entre peer
                        ServerSocket serverSocket = new ServerSocket();
                        // fazendo com que o serverSocket tenha mesma porta e IP que foi declarada pelo usuário
                        serverSocket.bind(endereco);
                        //transformando o IP em InetAddress
                        InetAddress address = serverSocket.getInetAddress();

                        String ipServer;
                        int portServer;
                        for(int n = 0; n < 3; n++){
                            scanner.nextLine();
                            System.out.println("Qual o endereço de IP do "+(n+1)+"° servidor: ");
                            ipServer = scanner.nextLine();
                            System.out.println("Qual a porta do "+(n+1)+"° servidor: ");
                            portServer = scanner.nextInt();
                            servers.add(new InetSocketAddress(ipServer, portServer));
                        }
                        //System.out.println(servers);
                        
                    }
                    
                } catch (IOException e) {
                    porta = -1;
                    address_str = null;
                    e.printStackTrace();
                }  
            }
            //se o usuário digitou 1 [PUT]
            
            else if(input == 1){
                
                //verificando se antes o usuário realizou requisição INIT
                if(porta == -1 && address_str == null){
                    System.out.println("Você ainda não se realizou a inicialização, faça isso e depois tente realizar o PUT.");
                } else {
                    // resgata informações necessárias para realizar o SEARCH
                    scanner.nextLine();
                    System.out.println("Qual a chave?");
                    int key = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Qual o valor?");
                    String value = scanner.nextLine();
                    // Chamada para método que realiza requisição SEARCH
                    try {
                        putRequest(key, value);
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
            }
            //se o usuário digitou 2 [GET]
            else if(input == 2){
                // verificando se antes o usuário realizou requisição INIT
                if (porta == -1 && address_str == null){
                    System.out.println("Você ainda não se realizou a inicialização, faça isso e depois tente realizar o GET.");
                }
                else{
                    // resgata informações necessárias para realizar o GET
                    scanner.nextLine();
                    System.out.println("Informe a chave: ");
                    int key = scanner.nextInt();
                    Instant timestamp = null;
                    if(retrieveValue(hashTableKV, key) != null){
                        timestamp = retrieveValue(hashTableKV, key).values().stream().findFirst().orElse(null);
                    }
                    
                    getRequest(key, timestamp);
                }
            }
            
            //se o usuário digitou 3 [LEAVE]
            else if(input == 3){
                condicao = false;
                System.exit(0);
            }
            // tratar opções inválidas
            else{
                System.out.println("Opção inválida");
            }

        }

        scanner.close();
        
    }


    private static void getRequest(int key, Instant timestamp) throws UnknownHostException, IOException, ClassNotFoundException {
        
        // Mensagem que queremos enviar por PUT 
        Mensagem msgGet = new Mensagem("GET", key, timestamp, new InetSocketAddress(IP, PORTA));
        // Recuperando tamanho da lista
        int tamanhoLista = servers.size();
        // Crie um objeto da classe Random
        Random random = new Random();
        // Gerando um número aleatório entre 0 (inclusive) e o tamanho da lista (exclusive)
        int idxServerPicked = random.nextInt(tamanhoLista);
        // Servidor escolhido
        InetSocketAddress server_end = servers.get(idxServerPicked);
        // Socket para conexão TCP entre cliente e servidor
        Socket s = new Socket(server_end.getHostString(), server_end.getPort());
        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        // Serializa o objeto e envia para o servidor
        out.writeObject(msgGet);
        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        // Recebe o objeto transmitido e realiza a deserialização
        Mensagem msgReturn = (Mensagem) in.readObject();
        // Caso a resposta seja PUT_OK, imprima o resultado, caso contrário, imprima um erro.
        if (msgReturn.getStatus().equals("GET_OK")){
            System.out.println("GET key: ["
            + msgGet.getKey()+"] value: ["
            + msgReturn.getValue()+"] obtido do servidor ["
            + server_end.getHostString() + ":" + server_end.getPort() +"], meu timestamp ["
            + msgGet.gettimestamp() +"] e do servidor ["
            + msgReturn.gettimestamp()+"]");
        } else if (msgReturn.getStatus().equals("TRY_OTHER_SERVER_OR_LATER")){
            if(retrieveValue(hashTableKV, msgGet.getKey()) != null) {
                String old_value = retrieveValue(hashTableKV, msgGet.getKey()).keySet().stream().findFirst().orElse(null);
                System.out.println("GET key: ["
                + msgGet.getKey()+"] value: ["
                + msgReturn.getValue()+"] obtido do servidor ["
                + server_end.getHostString() + ":" + server_end.getPort()  +"], meu timestamp ["
                + msgGet.gettimestamp() +"] e do servidor ["
                + msgReturn.gettimestamp()+"]");
            } else {
                System.out.println(msgReturn.getStatus());
            }
        }
        // fechando canal de entrada
        in.close();
        // fechando canal de saída
        out.close();
        // fechando socket TCP entre cliente e servidor
        s.close();
        
    }

    private static void putRequest(int key, String value) throws UnknownHostException, IOException, ClassNotFoundException {
        
        // Mensagem que queremos enviar por PUT 
        Mensagem msgPut = new Mensagem("PUT", key, value, new InetSocketAddress(IP, PORTA));
        // Recuperando tamanho da lista
        int tamanhoLista = servers.size();
        // Crie um objeto da classe Random
        Random random = new Random();
        // Gerando um número aleatório entre 0 (inclusive) e o tamanho da lista (exclusive)
        int idxServerPicked = random.nextInt(tamanhoLista);
        // Servidor escolhido
        InetSocketAddress server_end = servers.get(idxServerPicked);
        // Socket para conexão TCP entre cliente e servidor
        Socket s = new Socket(server_end.getHostName(), server_end.getPort());
        // Cria um ObjectOutputStream para enviar objetos a partir do OutputStream da conexão.
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        // Serializa o objeto e envia para o servidor
        out.writeObject(msgPut);
        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        // Recebe o objeto transmitido e realiza a deserialização
        Mensagem msgReturn = (Mensagem) in.readObject();
        // Caso a resposta seja PUT_OK, imprima o resultado, caso contrário, imprima um erro.
        // Além disso, adiciona esse elemento em nossa Hashtable local
        if (msgReturn.getStatus().equals("PUT_OK")){
            System.out.println("PUT_OK key: ["
                + msgPut.getKey()+"] value ["
                + msgPut.getValue() +"] timestamp ["
                + msgReturn.gettimestamp()+"] realizada no servidor ["
                + server_end.getHostString()+":"
                + server_end.getPort()+"]");
            // Adicionar KV à nossa hashtable local
            addData(key, value, msgReturn.gettimestamp());
        } else {
            System.out.println("Operação de PUT falhou! Tente novamente depois.");
        }
        // fechando canal de entrada
        in.close();
        // fechando canal de saída
        out.close();
        // fechando socket TCP entre cliente e servidor
        s.close();
        
    }

    public static void addData(int key, String value, Instant timestamp) {
        // Verifica se a tabela hash interna (interna ao primeiro nível) já existe
        hashTableKV.put(key, new Hashtable<>());
        // Adiciona o valor na tabela hash interna (interna ao primeiro nível)
        Hashtable<String, Instant> innerHashtable = hashTableKV.get(key);
        innerHashtable.put(value, timestamp);

        System.out.println(hashTableKV);
    }

    public static Hashtable<String, Instant> retrieveValue (Hashtable<Integer,
                                Hashtable<String, Instant>> hashtable, int key) {
        if (hashtable.containsKey(key)) {
            Hashtable<String, Instant> innerHashtable = hashtable.get(key);
            return innerHashtable;
        } else {
            return null;
        }
    }   
}

