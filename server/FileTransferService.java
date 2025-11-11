package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 3: Object Serialization & File Transfer
 * Demonstrates: Serialization, ObjectInputStream/OutputStream, File I/O
 * Contributor: Member 3
 */
public class FileTransferService implements Runnable {
    private static final int FILE_PORT = 5004;
    private static final String UPLOAD_DIR = "auction_files/";
    private static Map<Integer, AuctionFile> auctionFiles = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;

    public FileTransferService() throws IOException {
        serverSocket = new ServerSocket(FILE_PORT);
        
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        System.out.println("File Transfer Service started on port " + FILE_PORT);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleFileTransfer(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("File Transfer Service error: " + e.getMessage());
        }
    }

    private void handleFileTransfer(Socket socket) {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            
            String command = (String) ois.readObject();
            
            if (command.equals("UPLOAD")) {
                handleUpload(ois, oos);
            } else if (command.equals("DOWNLOAD")) {
                handleDownload(ois, oos);
            } else if (command.equals("LIST")) {
                handleList(oos);
            } else if (command.equals("SAVE_AUCTION_HISTORY")) {
                handleSaveHistory(ois, oos);
            } else if (command.equals("LOAD_AUCTION_HISTORY")) {
                handleLoadHistory(oos);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("File transfer error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private void handleUpload(ObjectInputStream ois, ObjectOutputStream oos) 
            throws IOException, ClassNotFoundException {
        AuctionFile auctionFile = (AuctionFile) ois.readObject();
        
        // Save file to disk
        String fileName = UPLOAD_DIR + auctionFile.getFileName();
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(auctionFile.getFileData());
        }
        
        // Store metadata
        auctionFiles.put(auctionFile.getAuctionId(), auctionFile);
        
        System.out.println("File uploaded: " + auctionFile.getFileName() + 
                          " for auction #" + auctionFile.getAuctionId());
        
        oos.writeObject("SUCCESS|File uploaded successfully");
        oos.flush();
    }

    private void handleDownload(ObjectInputStream ois, ObjectOutputStream oos) 
            throws IOException, ClassNotFoundException {
        int auctionId = (Integer) ois.readObject();
        AuctionFile auctionFile = auctionFiles.get(auctionId);
        
        if (auctionFile != null) {
            String fileName = UPLOAD_DIR + auctionFile.getFileName();
            File file = new File(fileName);
            
            if (file.exists()) {
                byte[] fileData = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileData);
                }
                auctionFile.setFileData(fileData);
                
                oos.writeObject(auctionFile);
                System.out.println("File downloaded: " + auctionFile.getFileName());
            } else {
                oos.writeObject("ERROR|File not found");
            }
        } else {
            oos.writeObject("ERROR|No file for this auction");
        }
        oos.flush();
    }

    private void handleList(ObjectOutputStream oos) throws IOException {
        oos.writeObject(auctionFiles);
        oos.flush();
    }

    private void handleSaveHistory(ObjectInputStream ois, ObjectOutputStream oos) 
            throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        java.util.List<Auction> auctions = (java.util.List<Auction>) ois.readObject();
        
        String historyFile = UPLOAD_DIR + "auction_history_" + System.currentTimeMillis() + ".ser";
        try (ObjectOutputStream fileOos = new ObjectOutputStream(new FileOutputStream(historyFile))) {
            fileOos.writeObject(auctions);
        }
        
        System.out.println("Auction history saved: " + historyFile);
        oos.writeObject("SUCCESS|History saved to " + historyFile);
        oos.flush();
    }

    private void handleLoadHistory(ObjectOutputStream oos) throws IOException {
        File dir = new File(UPLOAD_DIR);
        File[] historyFiles = dir.listFiles((d, name) -> name.startsWith("auction_history_") && name.endsWith(".ser"));
        
        if (historyFiles != null && historyFiles.length > 0) {
            // Get most recent history file
            File latestFile = historyFiles[0];
            for (File f : historyFiles) {
                if (f.lastModified() > latestFile.lastModified()) {
                    latestFile = f;
                }
            }
            
            try (ObjectInputStream fileOis = new ObjectInputStream(new FileInputStream(latestFile))) {
                Object history = fileOis.readObject();
                oos.writeObject(history);
                System.out.println("Auction history loaded: " + latestFile.getName());
            } catch (ClassNotFoundException e) {
                oos.writeObject("ERROR|Failed to load history");
            }
        } else {
            oos.writeObject("ERROR|No history files found");
        }
        oos.flush();
    }

    public static void start() {
        try {
            FileTransferService service = new FileTransferService();
            new Thread(service, "FileTransferService").start();
        } catch (IOException e) {
            System.err.println("Failed to start File Transfer Service: " + e.getMessage());
        }
    }
}

/**
 * Serializable class for auction file metadata and data
 */
class AuctionFile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int auctionId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private byte[] fileData;
    private String uploader;
    private long uploadTime;

    public AuctionFile(int auctionId, String fileName, String fileType, byte[] fileData, String uploader) {
        this.auctionId = auctionId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.fileSize = fileData.length;
        this.uploader = uploader;
        this.uploadTime = System.currentTimeMillis();
    }

    // Getters and setters
    public int getAuctionId() { return auctionId; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileData() { return fileData; }
    public String getUploader() { return uploader; }
    public long getUploadTime() { return uploadTime; }
    
    public void setFileData(byte[] fileData) { 
        this.fileData = fileData;
        this.fileSize = fileData.length;
    }

    @Override
    public String toString() {
        return String.format("AuctionFile[auction=%d, file=%s, type=%s, size=%d bytes, uploader=%s]",
                           auctionId, fileName, fileType, fileSize, uploader);
    }
}
