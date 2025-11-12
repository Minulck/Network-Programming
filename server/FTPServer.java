package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
    private static final int FTP_PORT = 5010;
    private static final String UPLOAD_DIR = "uploads/";
    private static ServerSocket serverSocket;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static volatile boolean running = false;

    public static void start() {
        try {
            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            serverSocket = new ServerSocket(FTP_PORT);
            running = true;
            System.out.println("FTP Server started on port " + FTP_PORT);
            System.out.println("Upload directory: " + new File(UPLOAD_DIR).getAbsolutePath());

            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("FTP client connected: " + clientSocket.getRemoteSocketAddress());
                        threadPool.execute(new FTPHandler(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Failed to start FTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("FTP Server shutdown complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class FTPHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public FTPHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Read command
                String command = in.readUTF();
                System.out.println("FTP Command received: " + command);

                if (command.startsWith("UPLOAD|")) {
                    handleUpload(command);
                } else if (command.startsWith("LIST")) {
                    handleList();
                } else {
                    sendResponse("ERROR|Unknown command");
                }

            } catch (IOException e) {
                System.err.println("FTP Handler error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleUpload(String command) throws IOException {
            try {
                // Parse: UPLOAD|filename|filesize
                String[] parts = command.split("\\|");
                if (parts.length < 3) {
                    sendResponse("ERROR|Invalid upload command format");
                    return;
                }

                String filename = parts[1];
                long filesize = Long.parseLong(parts[2]);

                // Sanitize filename
                filename = sanitizeFilename(filename);
                
                // Generate unique filename to avoid conflicts
                String uniqueFilename = System.currentTimeMillis() + "_" + filename;
                File uploadFile = new File(UPLOAD_DIR + uniqueFilename);

                System.out.println("Receiving file: " + uniqueFilename + " (" + filesize + " bytes)");

                // Send acknowledgment
                sendResponse("OK|" + uniqueFilename);

                // Receive file data
                try (FileOutputStream fos = new FileOutputStream(uploadFile)) {
                    byte[] buffer = new byte[8192];
                    long totalReceived = 0;
                    int bytesRead;

                    while (totalReceived < filesize) {
                        int toRead = (int) Math.min(buffer.length, filesize - totalReceived);
                        bytesRead = in.read(buffer, 0, toRead);
                        if (bytesRead == -1) break;
                        fos.write(buffer, 0, bytesRead);
                        totalReceived += bytesRead;
                    }

                    System.out.println("File received successfully: " + uniqueFilename + " (" + totalReceived + " bytes)");
                    sendResponse("SUCCESS|File uploaded: " + uniqueFilename);
                }

            } catch (Exception e) {
                System.err.println("Upload error: " + e.getMessage());
                e.printStackTrace();
                sendResponse("ERROR|Upload failed: " + e.getMessage());
            }
        }

        private void handleList() throws IOException {
            File uploadDir = new File(UPLOAD_DIR);
            File[] files = uploadDir.listFiles();
            
            StringBuilder fileList = new StringBuilder("FILES|");
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.append(file.getName()).append(",");
                    }
                }
            }
            sendResponse(fileList.toString());
        }

        private void sendResponse(String response) throws IOException {
            out.writeUTF(response);
            out.flush();
        }

        private String sanitizeFilename(String filename) {
            // Remove path traversal attempts and invalid characters
            filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
            filename = filename.replaceAll("\\.\\.", "");
            return filename;
        }
    }
}
