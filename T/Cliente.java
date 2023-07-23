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
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class Cliente {
    public static ArrayList<Servidor> servers = new ArrayList<>();
    public static Scanner scanner;
    public static String IP;
    public static int PORTA;

    public static void main(String[] args) throws IOException {
        
        // inicializando a variável de scanner para realizarmos a leitura de teclado
        scanner = new Scanner(System.in);

        //chamando o método do Menu interativo
        menu();
        

    }

    public static void menu() throws IOException{
        
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
                            System.out.println("Qual o endereço de IP do "+(n+1)+"° servidor: ");
                            ipServer = scanner.nextLine();
                            System.out.println("Qual a porta do "+(n+1)+"° servidor: ");
                            portServer = scanner.nextInt();
                            servers.add(new Servidor(portServer, ipServer, false));
                        }
                        
                        
                        // Criando thread que será responsável por escutar requisições do serverSocket 
                        // fazendo com que o peer sempre seja capaz de receber solicitação de outros peer
                        ThreadClient th = new ThreadClient(serverSocket);
                        th.start();
                    }
                    
                } catch (ServerNotActiveException | IOException | NotBoundException e) {
                    porta = -1;
                    address_str = null;
                    e.printStackTrace();
                }  
            }
            //se o usuário digitou 1 [PUT]
            else if(input == 1){
                //verificando se antes o usuário realizou requisição INIT
                if(porta == -1 && address_str == null){
                    System.out.println("Você ainda não se realizou a inicialização, faça isso e depois tente realizar o GET.");
                } else {
                    // resgata informações necessárias para realizar o SEARCH
                    scanner.nextLine();
                    System.out.println("Qual a chave?");
                    int key = scanner.nextInt();
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
                //verificando se antes o usuário realizou requisição INIT
                if (porta == -1 && address_str == null){
                    System.out.println("Você ainda não se realizou a inicialização, faça isso e depois tente realizar o GET.");
                }
                else{
                    // resgata informações necessárias para realizar o GET
                    scanner.nextLine();
                    System.out.println("Informe a chave: ");
                    int key = scanner.nextInt();
                    Instant timestamp = Instant.now();
                    
                    getRequest(key, timestamp);
                }
            }
            /*
            //se o usuário digitou 2 [DOWNLOAD]
            else if(input == 2){
                
                // verifica se já realizamos o JOIN
                if(searchFile == null){
                    System.out.println("Você ainda não pesquisou por nenhum arquivo, faça uma pesquisa antes de fazer o download");
                }
                // verifica se já realizamos o SEARCH
                else if (porta == -1 && address_str == null){
                    System.out.println("Você ainda não se juntou ao Napster, se junte e depois faça o download!");
                }
                else{
                    // resgata informações necessárias para realizar o DOWNLOAD
                    scanner.nextLine();
                    System.out.println("Qual o IP do peer que tem esse arquivo?");
                    String ipStrSearch = scanner.nextLine(); // IP do peer que tem os arquivos
                    
                    System.out.println("Qual a porta do peer que tem esse arquivo?");
                    int portaSearch = scanner.nextInt(); //porta o peer que tem os arquivos
                    //chamando método que requisita o DOWNLOAD
                    downloadRequest(searchFile, ipStrSearch, portaSearch);
                }
            }
            */
            // //se o usuário digitou 3 [LEAVE]
            else if(input == 3){
                condicao = false;
                // excluir aparições desse Peer dentro do servidor RMI
                System.exit(0);
            }
            // tratar opções inválidas
            else{
                System.out.println("Opção inválida");
            }

        }

        scanner.close();
        
    }


    private static void getRequest(int key, Instant timestamp) {

        // Mensagem que queremos enviar por PUT 
        Mensagem msgPut = new Mensagem("GET", key, timestamp);
        // Recuperando tamanho da lista
        int tamanhoLista = servers.size();
        // Crie um objeto da classe Random
        Random random = new Random();
        // Gerando um número aleatório entre 0 (inclusive) e o tamanho da lista (exclusive)
        int idxServerPicked = random.nextInt(tamanhoLista);
        // Servidor escolhido
        Servidor server = servers.get(idxServerPicked);

        Socket s = new Socket(server.getIpAddress(), server.getPort());

        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

        // Serializa o objeto e envia para o servidor
        out.writeObject(msgPut);
    }

    private static void putRequest(int key, String value) throws UnknownHostException, IOException, ClassNotFoundException {
        
        // Mensagem que queremos enviar por PUT 
        Mensagem msgPut = new Mensagem("PUT", key, value);
        // Recuperando tamanho da lista
        int tamanhoLista = servers.size();
        // Crie um objeto da classe Random
        Random random = new Random();
        // Gerando um número aleatório entre 0 (inclusive) e o tamanho da lista (exclusive)
        int idxServerPicked = random.nextInt(tamanhoLista);
        // Servidor escolhido
        Servidor server = servers.get(idxServerPicked);

        Socket s = new Socket(server.getIpAddress(), server.getPort());

        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

        // Serializa o objeto e envia para o servidor
        out.writeObject(msgPut);

        //espera resposta do servidor
        // Cria um ObjectInputStream para receber objetos a partir do InputStream da conexão.
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        // Recebe o objeto transmitido e realiza a deserialização
        Mensagem msgReturn = (Mensagem) in.readObject();
        if (msgReturn.getStatus().equals("PUT_OK")){
            // Imprime mensagem recebida
            System.out.println("PUT_OK key: ["
                + msgPut.getKey()+"] value ["
                + msgPut.getValue() +"] timestamp ["
                + msgReturn.getTimestampMillis()+"] realizada no servidor ["
                + server.getIpAddress()+":"
                + server.getPort()+"]");
        } else {
            System.out.println("Operação de PUT falhou! Tente novamente depois.");
        }
        

        in.close();
        out.close();
        s.close();
        
    }


    public static class ThreadClient extends Thread{
        
        //Server socket utilizado nessa conexão TCP
        public static ServerSocket serverSocket;

        /**
         * Construtor da classe
         * @param ss serverSocket utilizado pelo peer para realizar a conexão TCP 
         */
        public ThreadClient(ServerSocket ss) {
            serverSocket = ss;
        }

        /**
         * Rodando a thread que irá escutar as solicitações bem como realizar as transferências.
         */
        public void run(){
            try{

                while(true) {
                    Socket no = serverSocket.accept(); // Espera por uma conexão

                    //thread para realizar trasferência entre os peer
                    Thread th_accept = new Thread(() -> {
                        try (InputStreamReader is = new InputStreamReader(no.getInputStream())) {
                            BufferedReader reader = new BufferedReader(is); // fluxo de entrada de caracteres
                            // salva nome do arquivo solitiado
                            String fileName = reader.readLine();

                            // o peer tem esse arquivo solicitado?
                            boolean estaPresente = arquivos.contains(fileName);
                            
                            // fluxo de saída
                            OutputStream os = no.getOutputStream();
                            DataOutputStream writer = new DataOutputStream(os);

                            // Se o arquivo existe, confirme isso pro peer solicitante e envie o arquivo
                            if (estaPresente) {
                                writer.writeBytes("OK" + '\n');
                                String filePath = path + '\\' + fileName;
                                sendFile(filePath, no);
                            } else {
                                //se esse peer não tiver esse arquivo avise o peer solicitante
                                // cria a cadeia de saída (escrita) de informações do socket
                                writer.writeBytes("NOK" + '\n');
                            }
                        } catch (IOException e) {
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

