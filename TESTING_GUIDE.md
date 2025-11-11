# 🚀 Quick Testing Guide - All Features

## Test Each Feature Individually

### ✅ **Feature 1: Chat System (NIO)**

**Implemented by: Member 1**

```java
// Simple Java test client for Chat
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ChatTestClient {
    public static void main(String[] args) throws Exception {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 5002));

        // Login
        String login = "CHAT_LOGIN|TestUser\n";
        channel.write(ByteBuffer.wrap(login.getBytes()));

        // Send message
        String msg = "CHAT_MSG|Hello from test client!\n";
        channel.write(ByteBuffer.wrap(msg.getBytes()));

        // Read response
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        buffer.flip();
        System.out.println("Response: " + StandardCharsets.UTF_8.decode(buffer));

        Thread.sleep(5000);
        channel.close();
    }
}
```

**Expected Output:**

```
Chat [TestUser]: Hello from test client!
```

---

### ✅ **Feature 2: UDP Notifications**

**Implemented by: Member 2**

```java
// UDP listener test client
import java.net.*;

public class UDPTestClient {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5003);

        // Subscribe to notifications
        byte[] subscribeMsg = "SUBSCRIBE".getBytes();
        InetAddress serverAddr = InetAddress.getByName("localhost");
        DatagramPacket subscribePacket = new DatagramPacket(
            subscribeMsg, subscribeMsg.length, serverAddr, 5003);
        socket.send(subscribePacket);

        System.out.println("Subscribed to UDP notifications. Listening...");

        // Listen for notifications
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String notification = new String(packet.getData(), 0, packet.getLength());
            System.out.println("📢 Notification: " + notification);
        }
    }
}
```

**Expected Notifications:**

```
📢 Notification: NEW_AUCTION|iPhone 15|Starting at $500.00
📢 Notification: HIGH_BID|Auction #0|Alice bid $550.00
📢 Notification: BIDDING_WAR|Auction #0 (iPhone 15)|5 bids in last minute!
```

---

### ✅ **Feature 3: File Transfer & Serialization**

**Implemented by: Member 3**

```java
// File upload test client
import java.io.*;
import java.net.Socket;

public class FileTransferTestClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 5004);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        // Create test file
        byte[] testData = "Test auction image data".getBytes();
        AuctionFile file = new AuctionFile(0, "test.jpg", "image/jpeg",
                                          testData, "TestUser");

        // Upload
        oos.writeObject("UPLOAD");
        oos.writeObject(file);
        oos.flush();

        // Get response
        String response = (String) ois.readObject();
        System.out.println("Upload Response: " + response);

        socket.close();
    }
}
```

**Expected Output:**

```
File uploaded: test.jpg for auction #0
Upload Response: SUCCESS|File uploaded successfully
```

---

### ✅ **Feature 4: SSL/TLS Secure Connection**

**Implemented by: Member 4**

```java
// Secure connection test client
import javax.net.ssl.*;
import java.io.*;

public class SecureTestClient {
    public static void main(String[] args) throws Exception {
        // Trust all certificates (for testing only!)
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {}
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        SSLSocketFactory factory = sc.getSocketFactory();

        SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 5005);
        SSLSession session = socket.getSession();

        System.out.println("Connected securely!");
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("Protocol: " + session.getProtocol());

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        out.writeUTF("LOGIN|SecureUser");
        String welcome = in.readUTF();
        System.out.println("Server: " + welcome);

        socket.close();
    }
}
```

**Expected Output:**

```
Connected securely!
Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
Protocol: TLSv1.3
Server: WELCOME|SecureUser (Secure Connection)
```

---

### ✅ **Feature 5: Connection Pool Statistics**

**Implemented by: Member 5**

**Monitor in Server Console:**

```
=== Connection Pool Statistics ===
Uptime: 120 seconds
Active threads: 8
Pool size: 10/50
Queue size: 2/100
Active connections: 8
Total served: 45
Rejected: 0
Unique IPs: 5
Completed tasks: 37
Avg connections/sec: 0.375
===================================
```

**Test High Load:**

```java
// Stress test - create many connections
public class LoadTest {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            final int clientNum = i;
            new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", 5000);
                    System.out.println("Client " + clientNum + " connected");
                    Thread.sleep(10000); // Hold connection
                    socket.close();
                } catch (Exception e) {
                    System.err.println("Client " + clientNum + " rejected");
                }
            }).start();
            Thread.sleep(100); // Stagger connections
        }
    }
}
```

---

## 📊 Complete Feature Testing Checklist

### **Before Presentation:**

- [ ] **Server Startup**

  - [ ] All 7 services start without errors
  - [ ] Ports 5000-5005, 8080 available
  - [ ] Keystore generated (server.keystore)
  - [ ] auction_files/ directory created

- [ ] **Feature 1: Chat (NIO)**

  - [ ] Two users can send messages
  - [ ] Private messages work
  - [ ] System announcements appear
  - [ ] No thread-per-connection overhead

- [ ] **Feature 2: UDP Notifications**

  - [ ] New auction broadcast works
  - [ ] High bid notifications sent
  - [ ] Bidding war detected (5+ bids)
  - [ ] Ending soon warning (10 sec)
  - [ ] Auction ended notification

- [ ] **Feature 3: File Transfer**

  - [ ] File upload succeeds
  - [ ] File stored in auction_files/
  - [ ] File download works
  - [ ] Auction history saved/loaded

- [ ] **Feature 4: SSL/TLS**

  - [ ] Secure connection established
  - [ ] Cipher suite displayed
  - [ ] Self-signed certificate works
  - [ ] Data encrypted in transit

- [ ] **Feature 5: Connection Pool**

  - [ ] Statistics printed every 30 sec
  - [ ] Pool scales under load
  - [ ] Per-IP limits enforced
  - [ ] Rejected connections handled

- [ ] **Web Dashboard**
  - [ ] Three users in different tabs
  - [ ] Real-time bid updates
  - [ ] Countdown timers work
  - [ ] Notifications appear
  - [ ] Winner announced at end

---

## 🎯 Demo Scenarios for Presentation

### **Scenario 1: Full Auction Workflow (Main Demo)**

1. Start server (show all services)
2. Open 3 browser tabs → Login Alice, Bob, Charlie
3. Alice creates "iPhone 15 Pro" auction ($500, 60 sec)
4. Show UDP broadcast in console
5. Bob opens chat (Feature 1) → "Hi everyone!"
6. Alice uploads iPhone image (Feature 3)
7. Charlie connects via secure SSL (Feature 4)
8. Bob bids $550 → Show UDP notification
9. Alice bids $600 → Another notification
10. Rapid bidding → Bidding war alert!
11. 10 seconds left → Ending soon alert
12. Auction ends → Winner announced
13. Show connection pool stats (Feature 5)

### **Scenario 2: High Load Test (Optional)**

1. Run load test script (100 connections)
2. Show connection pool scaling
3. Show statistics (active threads, queue size)
4. Show rejection when limit reached
5. Demonstrate graceful degradation

### **Scenario 3: Security Demo (Optional)**

1. Normal connection on port 5000
2. Secure connection on port 5005
3. Compare using Wireshark (encrypted vs plain)
4. Show certificate details

---

## 🔧 Troubleshooting

### **Problem: Port already in use**

```bash
# Windows - Find and kill process
netstat -ano | findstr :5000
taskkill /PID <process_id> /F
```

### **Problem: Keystore not found**

```bash
# Generate manually
keytool -genkeypair -alias auctionserver -keyalg RSA -keystore server.keystore -storepass auction123 -keypass auction123 -dname "CN=BidEasy,OU=IT,O=BidEasy,L=City,ST=State,C=US"
```

### **Problem: Compilation errors**

```bash
# Ensure all files compiled
javac -encoding UTF-8 -cp "lib/*" server/*.java shared/*.java client/Client.java
```

### **Problem: WebSocket not connecting**

- Check browser console for errors
- Verify port 5001 is open
- Ensure java-websocket jar in lib/

---

## 📝 Quick Commands Reference

```bash
# Compile all
compile.bat

# Start server
start-server.bat

# Or manually
java -cp ".;lib/*" server.Server

# Test specific port
telnet localhost 5000

# Check all listening ports
netstat -an | findstr "5000 5001 5002 5003 5004 5005 8080"

# Generate keystore
keytool -genkeypair -alias auctionserver -keyalg RSA -keystore server.keystore -storepass auction123

# View keystore
keytool -list -keystore server.keystore -storepass auction123
```

---

## 🎬 Recording Demo Video (Optional)

If you want to create a backup demo video:

1. Start OBS Studio or similar
2. Record screen + audio
3. Run complete demo scenario
4. Show console output + web interface side-by-side
5. Narrate what's happening
6. Keep video under 5 minutes

---

## ✨ Presentation Day Checklist

**30 Minutes Before:**

- [ ] Test all equipment (projector, laptop)
- [ ] Start server early (verify all services)
- [ ] Open browser tabs (3 users logged in)
- [ ] Test chat, file upload, bidding
- [ ] Verify UDP listener running
- [ ] Check connection pool stats appearing
- [ ] Have backup screenshots ready
- [ ] Practice one full run-through

**During Presentation:**

- [ ] Speak clearly, don't rush
- [ ] Point to relevant console output
- [ ] Highlight real-time updates
- [ ] Explain "why" not just "what"
- [ ] Be ready for questions
- [ ] If demo breaks, explain expected behavior

**After Presentation:**

- [ ] Thank the audience
- [ ] Be ready for Q&A
- [ ] Have technical details ready
- [ ] Know your code well

---

**Good Luck! 🎯 You've got this!**

_Remember: Even if something goes wrong during the demo, what matters is your understanding of the concepts and your ability to explain them clearly._
