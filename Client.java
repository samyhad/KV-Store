import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Client {

    public static ArrayList<Address> servers = new ArrayList<>();
    public static Scanner scanner;
    public static Peer peer;

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
        Peer peer;

        //LOOP do menu interativo
        while(condicao == true){
            //imprimindo opções no terminal para o usuário poder escolher qual opção ele quer
            System.out.println("Qual a requisi\u00E7\u00E3o desejada? (apenas n\u00FAmero)");
            System.out.println("[0]: JOIN");
            System.out.println("[1]: PUT");
            System.out.println("[2]: GET");
            System.out.println("[3]: LEAVE");
            //lê entrada do usuário
            try{
                input = scanner.nextInt();
            }catch (InputMismatchException exception){
                System.out.println("Digite apenas valores inteiros por favor");
            }

            //se o usuário digitou 0 [JOIN]
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
                        
                        //criando objeto peer para manipular esse peer.
                        peer = new Peer(address_str, porta);
                        
                        
                        // Criando thread que será responsável por escutar requisições do serverSocket 
                        // fazendo com que o peer sempre seja capaz de receber solicitação de outros peer
                        th = new ThreadClient(serverSocket);
                        th.start();
                    }
                    // Lendo diretório que o Peer irá utilizar
                    if (path == null){
                        path = scanner.nextLine();
                        System.out.println("Qual o nome do diretório que se encontra os seus arquivos?");
                        path = scanner.nextLine();
                    }
                    // chamada para método que faz requisição JOIN
                    joinRequest();
                } catch (ServerNotActiveException | IOException | NotBoundException e) {
                    path = null;
                    porta = -1;
                    address_str = null;
                    e.printStackTrace();
                }  
            } 
            //se o usuário digitou 1 [SEARCH]
            else if(input == 1){
                //verificando se antes o usuário realizou requisição JOIN
                if(porta == -1 && address_str == null){
                    System.out.println("Você ainda não se juntou ao Napster, se junte e depois faça o download!");
                } else {
                    // resgata informações necessárias para realizar o SEARCH
                    scanner.nextLine();
                    System.out.println("Qual o nome do arquivo que você deseja procurar?");
                    searchFile = scanner.nextLine();
                    // Chamada para método que realiza requisição SEARCH
                    searchRequest(searchFile);
                }
                
            }
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
            // //se o usuário digitou 3 [LEAVE]
            else if(input == 3){
                condicao = false;
                // excluir aparições desse Peer dentro do servidor RMI
                if(address_str != null && porta != -1 && !arquivos.isEmpty()){
                    shc.DELETE(arquivos, peer);
                }
                System.exit(0);
            }
            // tratar opções inválidas
            else{
                System.out.println("Opção inválida");
            }

        }

        scanner.close();
        
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
