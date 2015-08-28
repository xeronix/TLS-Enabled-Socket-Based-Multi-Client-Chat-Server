import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
    private static int clientIdVal = 1;
    ArrayList<PrintWriter> clientWriter = new ArrayList<PrintWriter>();
    SSLServerSocketFactory sslServerSocketFactory;
    
    public void initSSL() throws Exception {
        String keyStoreName = "keystore.jks";
        char[] keyStorePass = "keystore".toCharArray();

        FileInputStream fin = new FileInputStream(keyStoreName);
        
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(fin, keyStorePass);
        
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyStorePass);
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        
        sslServerSocketFactory = sslContext.getServerSocketFactory();
    }
    
    public class ClientHandler implements Runnable {
        SSLSocket sslClientSocket;
        int clientId;
        
        public ClientHandler(SSLSocket socket, int id) {
            sslClientSocket = socket;
            clientId = id;
        }
        
        @Override
        public void run() {
            BufferedReader br = null;
            
            try {
                br = new BufferedReader(new InputStreamReader(sslClientSocket.getInputStream()));
                
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
        SSLServerSocket sslServerSocket = null;
        
        try {
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(5555);

            while (true) {
                try {
                    SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();
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
                if (sslServerSocket != null) {
                    sslServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String args[]) throws Exception {
        Server server = new Server();
        server.initSSL();
        server.start();
    }
}