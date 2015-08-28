import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class Client {
    PrintWriter pr;
    BufferedReader br;
    SSLSocketFactory sslSocketFactory;
    
    public void initSSL() throws Exception {
        String trustStoreName = "truststore.jks";
        char[] trustStorePass = "truststore".toCharArray();

        FileInputStream fin = new FileInputStream(trustStoreName);
        
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(fin, trustStorePass);
        
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        
        sslSocketFactory = sslContext.getSocketFactory();
    }
    
    public static void main(String args[]) throws Exception {
        Client client = new Client();
        client.initSSL();
        client.start();
    }
    
    private void start() {
        SSLSocket sslSocket = null;
        br = null;
        
        try {
            // server address is localhost:5555
            sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 5555);
            br = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            pr = new PrintWriter(sslSocket.getOutputStream());
            
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
                if (sslSocket != null) {
                    sslSocket.close();
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
