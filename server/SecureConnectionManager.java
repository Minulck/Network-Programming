package server;

import java.io.*;
import java.security.*;
import java.util.List;
import javax.net.ssl.*;
import shared.MessageSender;
import shared.Protocol;

/**
 * Feature 4: SSL/TLS Secure Connection
 * Demonstrates: SSLServerSocket, SSLSocket, KeyStore, TLS encryption
 * Contributor: Member 4
 */
public class SecureConnectionManager implements Runnable {
    private static final int SECURE_PORT = 5005;
    private static final String KEYSTORE_FILE = "server.keystore";
    private static final String KEYSTORE_PASSWORD = "auction123";
    private SSLServerSocket secureServerSocket;
    private List<MessageSender> senders;

    public SecureConnectionManager(List<MessageSender> senders) throws Exception {
        this.senders = senders;
        
        // Setup SSL context
        SSLContext sslContext = createSSLContext();
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        secureServerSocket = (SSLServerSocket) factory.createServerSocket(SECURE_PORT);
        
        // Enable only strong cipher suites
        String[] enabledCipherSuites = {
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_GCM_SHA256"
        };
        secureServerSocket.setEnabledCipherSuites(enabledCipherSuites);
        
        System.out.println("Secure SSL/TLS Server started on port " + SECURE_PORT);
        System.out.println("Enabled cipher suites: " + String.join(", ", enabledCipherSuites));
    }

    private SSLContext createSSLContext() throws Exception {
        // Check if keystore exists, if not, create it
        File keystoreFile = new File(KEYSTORE_FILE);
        if (!keystoreFile.exists()) {
            System.out.println("Creating self-signed certificate...");
            createSelfSignedCertificate();
        }

        // Load keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        // Setup key manager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // Setup trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    private void createSelfSignedCertificate() {
        try {
            String[] command = {
                "keytool",
                "-genkeypair",
                "-alias", "auctionserver",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "365",
                "-keystore", KEYSTORE_FILE,
                "-storepass", KEYSTORE_PASSWORD,
                "-keypass", KEYSTORE_PASSWORD,
                "-dname", "CN=BidEasy Auction Server, OU=IT, O=BidEasy, L=City, ST=State, C=US"
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Self-signed certificate created successfully");
            } else {
                System.err.println("Failed to create certificate. Manual creation required.");
                System.err.println("Run: keytool -genkeypair -alias auctionserver -keyalg RSA -keystore " + 
                                 KEYSTORE_FILE + " -storepass " + KEYSTORE_PASSWORD);
            }
        } catch (Exception e) {
            System.err.println("Certificate creation error: " + e.getMessage());
            System.err.println("Please create keystore manually using keytool command.");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) secureServerSocket.accept();
                
                // Print connection info
                SSLSession session = clientSocket.getSession();
                System.out.println("Secure client connected: " + clientSocket.getRemoteSocketAddress());
                System.out.println("Cipher Suite: " + session.getCipherSuite());
                System.out.println("Protocol: " + session.getProtocol());

                SecureClientHandler handler = new SecureClientHandler(clientSocket, senders);
                senders.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Secure connection error: " + e.getMessage());
        }
    }

    public static void start(List<MessageSender> senders) {
        try {
            SecureConnectionManager manager = new SecureConnectionManager(senders);
            new Thread(manager, "SecureConnectionManager").start();
        } catch (Exception e) {
            System.err.println("Failed to start Secure Connection Manager: " + e.getMessage());
            System.err.println("Secure connections will not be available.");
        }
    }
}

/**
 * Handler for secure SSL/TLS client connections
 */
class SecureClientHandler implements Runnable, MessageSender {
    private SSLSocket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private List<MessageSender> senders;

    public SecureClientHandler(SSLSocket socket, List<MessageSender> senders) {
        this.socket = socket;
        this.senders = senders;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Login
            String loginMsg = in.readUTF();
            if (loginMsg.startsWith(Protocol.LOGIN + "|")) {
                username = loginMsg.split("\\|")[1];
                out.writeUTF(Protocol.welcomeMessage(username) + " (Secure Connection)");
                System.out.println("Secure login: " + username);
            } else {
                out.writeUTF(Protocol.errorMessage("Invalid login"));
                return;
            }

            String input;
            while ((input = in.readUTF()) != null) {
                // All messages are encrypted by SSL/TLS
                AuctionManager.processMessage(input, this);
            }
        } catch (IOException e) {
            System.out.println("Secure client disconnected: " + username);
            senders.remove(this);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send to secure client: " + username);
            senders.remove(this);
        }
    }

    @Override
    public String getUsername() {
        return username;
    }
}
