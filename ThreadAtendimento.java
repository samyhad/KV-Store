import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class ThreadAtendimento extends Thread {
    private Socket serverSocket = null;

    public ThreadAtendimento(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            // Recebe o objeto transmitido e realiza a deserialização
            Mensagem msg = (Mensagem) in.readObject();
            System.out.println(msg);

            in.close();
            serverSocket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    //Para enviar 
    //ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    // Objeto que será transmitido
    //Pessoa pessoa = new Pessoa("João", 30);

    // Serializa o objeto e envia para o servidor
    //out.writeObject(pessoa);

    //out.close();
    //socket.close();
}
