import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static int clientIdVal = 1;
    ArrayList<PrintWriter> clientWriter = new ArrayList<PrintWriter>();
    
    public class ClientHandler implements Runnable {
        Socket clientSocket;
        int clientId;
        
        public ClientHandler(Socket socket, int id) {
            clientSocket = socket;
            clientId = id;
        }
        
        @Override
        public void run() {
            BufferedReader br = null;
            
            try {
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                String line = "";
                
                while ((line = br.readLine()) != null) {
                    System.out.println("Client " + clientId + ": " + line);
                    broadcastMessage("Client " + clientId + ": " + line, clientId);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        
        private void broadcastMessage(String message, int senderId) {
            int id = 0;

            for (PrintWriter pr : clientWriter) {
                if (id+1 != senderId) {
                    pr.println(message);
                    pr.flush();
                }
                
                id++;
            }
        }
    }
    
    private void start() {
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(5555);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());
                    clientWriter.add(pr);
                    
                    Runnable runnable = new ClientHandler(clientSocket, clientIdVal);
                    Thread t = new Thread(runnable);
                    t.start();
                    System.out.println("Client " + clientIdVal + " connected");
                    clientIdVal++;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String args[]) {
        Server server = new Server();
        server.start();
    }
}