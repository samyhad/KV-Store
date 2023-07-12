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

    public static void main(String[] args) throws Exception{

      Scanner scanner = new Scanner(System.in);
      //scanner.nextLine();
      //System.out.println("Qual o IP desse peer?");
      //address_str = scanner.nextLine();
      //System.out.println("Qual a porta desse peer?");
      //porta = scanner.nextInt();

      InetSocketAddress endereco = new InetSocketAddress(address_str, porta);
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.bind(endereco);
      //ServerSocket serverSocket = new ServerSocket(0);
      //porta = serverSocket.getLocalPort();
      InetAddress address = serverSocket.getInetAddress();

      
    }
}
