import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {
    PrintWriter pr;
    BufferedReader br;
    
    public static void main(String args[]) {
        Client client = new Client();
        client.start();
    }
    
    private void start() {
        Socket socket = null;
        br = null;
        
        try {
            socket = new Socket("localhost", 5555);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pr = new PrintWriter(socket.getOutputStream());
            
            BufferedReader inbr = new BufferedReader(new InputStreamReader(System.in));
            
            Runnable runnable = new ServerReader();
            Thread t = new Thread(runnable);
            t.start();
            
            String line = "";
            
            while ((line = inbr.readLine()) != null) {
                sendMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                } 

                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendMessage(String message) {
        pr.println(message);
        pr.flush();
    }

    public class ServerReader implements Runnable {
        @Override
        public void run() {
            String message = "";
            
            try {
                while ((message = br.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
