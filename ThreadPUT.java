public class ThreadPUT extends Thread{

    private Socket no = null;

    public ThreadPUT(Socket node){
        no = node;
    }

    public void run(){
        try{
            InputStreamReader is =  new InputStreamReader(no.getInputStream());
            BufferedReader reader = new BufferedReader(is);
            OutputStream os = no.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            String texto = reader.readLine();
            writer.writeBytes(texto.toUpperCase() + '\n');

        }catch(Exception e){
            //
        }

    }
    
    
}
