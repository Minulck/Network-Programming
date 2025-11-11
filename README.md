# 🎯 BidEasy: Real-Time Auction System

**A modern, professional real-time auction platform demonstrating advanced Java Network Programming concepts with beautiful web interface and WebSocket-powered live updates.**

---

## 🚀 Quick Start (30 Seconds)

### Method 1: One-Click Start ⚡

```
1. Double-click: start-server.bat
2. Open browser: http://localhost:8080
3. Start bidding!
```

### Method 2: PowerShell

```powershell
.\start-server.bat
```

### Method 3: Manual Start

```powershell
java -cp ".;lib\java-websocket-1.5.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar" server.Server
```

### What Happens When You Start:

- ✅ Server compiles automatically
- ✅ All 6 services start (Web, WebSocket, Console, Chat NIO, UDP, SSL/TLS)
- ✅ Web interface opens at http://localhost:8080

---

## 👥 Group Members and Contributions

This project was developed by a team of 5 members, each implementing distinct network programming features:

### Member 1: Java NIO Chat System

- **Focus**: Real-time private chat using Java NIO (Non-blocking I/O)
- **Port**: 5002
- **Technologies**: Selector, SelectionKey, SocketChannel, ServerSocketChannel, ByteBuffer
- **Files**: `server/ChatManager.java`, `server/Server.java`, `client/dashboard.html`
- **Features**: Non-blocking chat server, private messaging, user presence tracking, scalable connection handling

### Member 2: UDP Notification Service

- **Focus**: Connectionless messaging for instant notifications
- **Port**: 5003
- **Technologies**: DatagramSocket, DatagramPacket, UDP Broadcasting
- **Files**: `server/UDPNotificationService.java`
- **Features**: Broadcast notifications, subscription model, fast messaging for auction events (new auction, bidding war, ending soon, auction ended)

### Member 3: Multi-threading Server Architecture

- **Focus**: Concurrent client handling with thread-per-client model
- **Port**: 5000 (Console client TCP connections)
- **Technologies**: Java Threads, Runnable, CopyOnWriteArrayList
- **Files**: `server/ClientHandler.java`, `server/Server.java`
- **Features**: Thread-per-client model, concurrent auction operations, thread-safe collections, synchronized access to shared resources

### Member 4: Secure Connection Management

- **Focus**: SSL/TLS encryption for secure network communication
- **Port**: 5005
- **Technologies**: SSLServerSocket, SSLContext, KeyStore, TrustManager, KeyManager
- **Files**: `server/SecureConnectionManager.java`, `server.keystore`
- **Features**: TLS encryption, self-signed certificate generation, strong cipher suites, secure handshake, encrypted bidirectional communication

### Member 5: Client-Server Communication (Sockets)

- **Focus**: TCP socket communication and WebSocket real-time updates
- **Ports**: 5000 (TCP), 5001 (WebSocket), 8080 (HTTP)
- **Technologies**: ServerSocket, Socket, DataInputStream/DataOutputStream, WebSocket, HTTP Server
- **Files**: `server/Server.java`, `client/Client.java`, `shared/Protocol.java`
- **Features**: Reliable TCP connections, bidirectional communication, WebSocket for real-time web updates, HTTP server for web interface, protocol-based messaging

---

## 🌐 Network Programming Concepts Demonstrated

This application showcases comprehensive Java network programming techniques:

### 1. **Java NIO (Non-blocking I/O)**

- Non-blocking socket operations with Selector and SelectionKey
- Efficient handling of multiple concurrent connections
- Scalable chat system implementation

### 2. **UDP Protocol**

- Connectionless, lightweight messaging
- Fast broadcasting for notifications
- Best-effort delivery for non-critical data

### 3. **Multi-threading**

- Thread-per-client architecture for concurrent users
- Synchronization mechanisms for thread safety
- Resource management in multi-threaded environment

### 4. **SSL/TLS Security**

- Encrypted network communication
- Certificate-based authentication
- Secure data transmission

### 5. **Socket Programming (TCP)**

- Reliable, connection-oriented communication
- Bidirectional data exchange between clients and server
- Connection management and error handling

---

## ✨ Features

- 🌐 **Beautiful Web Dashboard** - Modern, responsive interface with smooth animations
- ⚡ **Real-time Updates** - Instant bid updates across all users via WebSocket
- � **Private Chat System** - Real-time messaging using Java NIO (Non-blocking I/O)
- �👥 **Multi-user Ready** - Unlimited concurrent users can participate
- ⏱️ **Auto-ending Auctions** - Countdown timers with automatic auction closure
- 📱 **Mobile Friendly** - Perfect on phones, tablets, and desktops
- 🎮 **Easy Controls** - Quick bid buttons and intuitive interface
- 🖥️ **Console Alternative** - Command-line client for advanced users
- 🔔 **UDP Notifications** - Fast broadcast alerts for auction events
- 🔒 **SSL/TLS Security** - Encrypted connections for secure communication
- 📊 **Activity Tracking** - Personal auction and bid history

---

## 🎮 Quick Demo (2 Minutes)

### Basic Demo:

1. **Start Server** - Run `start-server.bat`
2. **Open Browser** - Go to http://localhost:8080
3. **Login** - Enter username "Alice"
4. **Create Auction** - Click "➕ Create Auction" tab
   - Name: "iPhone 15 Pro"
   - Starting Price: 500
   - Duration: 60 seconds
5. **Second User** - Open new tab, login as "Bob"
6. **Bid** - Go to "🏆 Active Auctions" → Click "Quick Bid $501"
7. **Watch Magic** - See real-time updates everywhere!

### Advanced Demo Scenarios:

#### Competitive Bidding:

- Have 3+ users compete on one auction
- Watch bid escalation in real-time
- See who wins when timer expires

#### Multiple Auctions:

- Create several auctions with different durations
- Watch them end at different times
- See how users manage multiple auctions

#### Console + Web Mix:

- Start console client: `java client.Client`
- Use commands: `CREATE|Laptop|800|90`
- Mix web and console users!

---

## 🎪 Dashboard Features

### 🏆 Active Auctions Tab

- View all live auctions in real-time
- Quick bid buttons for easy bidding
- Live countdown timers
- Instant winner announcements

### ➕ Create Auction Tab

- Simple form to start new auctions
- Set name, starting price, duration
- Instant creation and broadcasting

### 💰 Place Bid Tab

- Manual bidding by auction ID
- Input validation and error handling
- Success notifications

### � Chat (Java NIO) Tab

- Real-time private messaging between users
- User presence indicators and online status
- Message history and unread message notifications
- Non-blocking I/O implementation for scalability

### �📊 My Activity Tab

- Track your created auctions
- View your bidding history
- Personal activity summary

---

## 🏗️ Project Structure

```
📁 RealTimeAuctionSystem/
├── 🚀 start-server.bat      ← ONE-CLICK STARTUP
├── 🔧 compile.bat           ← Compile if needed
├── 📁 client/
│   ├── dashboard.html       ← Modern unified web interface
│   ├── styles.css          ← Beautiful styling
│   └── Client.java         ← Console client
├── 📁 server/              ← Java server code (6 files)
│   ├── Server.java         ← Main server with WebSocket
│   ├── ChatManager.java    ← NIO Chat System (Member 1)
│   ├── AuctionManager.java ← Auction logic
│   ├── ClientHandler.java  ← Console client handler
│   ├── UDPNotificationService.java ← UDP Broadcasting (Member 2)
│   ├── SecureConnectionManager.java ← SSL/TLS Security (Member 4)
│   └── Auction.java        ← Auction data model
├── 📁 shared/              ← Common protocols (2 files)
│   ├── Protocol.java       ← Message protocols
│   └── MessageSender.java  ← Message interface
└── 📁 lib/                 ← All dependencies included (3 jars)
    ├── java-websocket-1.5.3.jar
    ├── slf4j-api-1.7.36.jar
    └── slf4j-simple-1.7.36.jar
```

---

## 🌐 Server Ports

The system demonstrates 6 distinct network programming concepts on separate ports:

- **Port 8080** - HTTP Server (Web interface) - Member 5
- **Port 5000** - TCP Socket Server (Console clients) - Member 3 & 5
- **Port 5001** - WebSocket Server (Real-time web updates) - Member 5
- **Port 5002** - Java NIO Chat Server (Non-blocking I/O) - Member 1
- **Port 5003** - UDP Notification Service (Broadcasting) - Member 2
- **Port 5005** - SSL/TLS Secure Server (Encrypted connections) - Member 4

---

## 🔧 Requirements

- ☕ **Java 8+** (required) - Install from [java.com](https://java.com/download)
- 🌐 **Modern Web Browser** (Chrome, Firefox, Safari, Edge)
- 🖥️ **Ports Available**: 5000, 5001, 5002, 5003, 5005, 8080
- 💾 **Windows** (batch files included) or adapt for Mac/Linux

---

## 🎯 Multi-User Testing

Open multiple browser tabs/windows:

1. **Tab 1**: Login as "Alice" → Create auctions
2. **Tab 2**: Login as "Bob" → Place bids
3. **Tab 3**: Login as "Charlie" → Compete with Bob
4. **Watch**: Real-time updates across all tabs!

---

## 🖥️ Console Client (Alternative)

```bash
java client.Client
```

**Commands:**

- `CREATE|iPhone|500|60` - Create auction
- `BID|0|550` - Bid on auction ID 0
- `exit` - Quit

---

## 🛠️ Troubleshooting

### Server Won't Start

- **No Java?** → Install from [java.com](https://java.com/download)
- **Port busy?** → Close other applications using ports 5000, 5001, 5002, 5003, 5005, 8080
- **Permission denied?** → Run as Administrator
- **Compilation failed?** → Check Java version with `java -version`

### Web Page Won't Load

- Ensure server shows "HTTP server started on port 8080"
- Try direct URL: http://localhost:8080/dashboard.html
- Clear browser cache and cookies
- Check Windows Firewall settings

### Connection Issues

- Try different browser (Chrome recommended)
- Disable browser extensions temporarily
- Restart server completely
- Check antivirus software isn't blocking connections

### Common Solutions

- Restart your computer
- Make sure no other servers are running on the same ports
- Verify all jar files are in the `lib/` folder

---

## 🎯 Architecture & Technology

### What Makes This Special:

1. **Real-time Everything** - WebSocket-powered instant updates
2. **Advanced Chat System** - Java NIO-based private messaging
3. **Modern UI** - Clean, professional interface with gradients and animations
4. **Multi-user Ready** - Unlimited concurrent users
5. **Mobile Friendly** - Works perfectly on phones and tablets
6. **Easy Setup** - One-click server startup
7. **Professional Code** - Clean Java architecture with proper error handling

### Technical Stack:

- **Backend**: Java with WebSocket server
- **Frontend**: Modern HTML5, CSS3, JavaScript
- **Communication**: WebSocket for real-time updates, Java NIO for chat
- **Architecture**: Multi-threaded server with event-driven client updates
- **Security**: SSL/TLS encrypted connections
- **Networking**: TCP sockets, UDP broadcasting, Java NIO, multi-threading

---

## 📋 Project Documentation

### Report and Presentation Materials

- **WORK_DISTRIBUTION.md**: Detailed work allocation for team members
- **REPORT_README.md**: Complete report template with all required sections
- **PRESENTATION_README.md**: Presentation guide and slide templates

### Report Sections (as per assignment requirements):

- ✅ Project Title
- ✅ Group Members and Individual Contributions
- ✅ System Overview
- ✅ Network Programming Concepts Used
- ✅ Screenshots of Outputs
- ✅ Challenges Faced and Solutions
- ✅ Conclusion

### Presentation Requirements:

- 10-minute presentation + 5-minute Q&A
- Each member presents their network programming feature
- Physical demonstration of the running application
- No formal attire required

---

## 🎉 Ready to Go!

Everything is included and configured. Just run `start-server.bat` and enjoy your professional auction system!

**Perfect for:**

- 🎓 Learning WebSocket programming
- 📊 Demonstrating real-time applications
- 🎮 Fun competitive bidding games
- 📚 Educational projects
- 💼 Portfolio showcases
- 🏢 Network programming demonstrations

**Happy Bidding! 🎯**
