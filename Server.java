import java.util.ArrayList;
import java.util.Scanner;
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
    private int port;
    private String ipAddress;
    private Boolean isLeader;
    private InetSocketAddress 

    private static void setPort (int port){
        this.port = port;
    }

    private static int getPort (
        return this.port;
    ) 

    private static void setIpAddress (int ipAddress){
        this.ipAddress = ipAddress;
    }

    private static int getIpAddress (
        return this.ipAddress;
    ) 

    public static void main(String[] args) throws Exception{

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("Qual o IP desse servidor?");
        setIpAddress(scanner.nextLine());
        System.out.println("Qual a porta desse servidor?");
        setPort(scanner.nextInt());

        InetSocketAddress endereco = new InetSocketAddress(getIpAddress(), getPort());
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(endereco);
        
        //ServerSocket serverSocket = new ServerSocket(10099);
        
        //10097, 10098 e 10099

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
            ThreadPUT thread = new ThreadPUT(no);
            thread.start(); //executa a thread -> chamada ao método run()
        }
      
    }
}
