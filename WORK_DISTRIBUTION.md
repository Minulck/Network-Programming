# Work Distribution for Real-Time Auction System

## Group Members and Contributions

### Member 1: [Name] - Chat System with Java NIO

- **Feature**: Real-time private chat using Java NIO (Non-blocking I/O)
- **Network Concept**: Non-blocking I/O with Selector, SelectionKey, SocketChannel, ServerSocketChannel, and ByteBuffer
- **Port**: 5002
- **Files Modified**: `server/ChatManager.java`, `server/Server.java`, `client/dashboard.html`
- **Key Technologies**:
  - java.nio.channels.Selector for event-driven I/O
  - ServerSocketChannel and SocketChannel for non-blocking connections
  - ByteBuffer for efficient data handling
  - ConcurrentHashMap for thread-safe user management
- **Description**: Implemented a scalable chat system using Java NIO for efficient handling of multiple concurrent chat connections. The system uses a single-threaded Selector to manage multiple channels, demonstrating the power of non-blocking I/O. Features include private messaging between users, user presence tracking, and broadcast messaging.

### Member 2: [Name] - UDP Notification Service

- **Feature**: UDP-based notification broadcasting
- **Network Concept**: User Datagram Protocol for fast, connectionless notifications
- **Port**: 5003
- **Files Modified**: `server/UDPNotificationService.java`, `server/Server.java`
- **Key Technologies**:
  - DatagramSocket for UDP communication
  - DatagramPacket for message encapsulation
  - Broadcast addressing (255.255.255.255)
  - Subscription listener for client registration
- **Description**: Implemented UDP broadcasting for instant notifications about auction events. The service operates on a connectionless protocol, providing fast, lightweight messaging suitable for non-critical updates. Features include subscription management, multiple notification types (new auction, bidding war, ending soon, auction ended, high bid), and broadcast to all registered subscribers.

### Member 3: [Name] - Multi-threading Server Architecture

- **Feature**: Multi-threaded client handling for concurrent console users
- **Network Concept**: Multi-threading to handle multiple simultaneous client connections
- **Port**: 5000 (TCP socket connections)
- **Files Modified**: `server/ClientHandler.java`, `server/Server.java`
- **Key Technologies**:
  - Java Thread API for concurrent execution
  - Runnable interface implementation
  - CopyOnWriteArrayList for thread-safe client list management
  - Synchronized blocks for shared resource access
- **Description**: Implemented thread-per-client model allowing multiple console users to connect and interact with the auction system simultaneously. Each client connection spawns a dedicated thread, enabling true concurrent operations. The implementation demonstrates proper thread lifecycle management, thread-safe data structures, and prevention of race conditions in auction operations.

### Member 4: [Name] - Secure Connection Management

- **Feature**: SSL/TLS encryption for secure communications
- **Network Concept**: Secure Socket Layer/Transport Layer Security for encrypted network communication
- **Port**: 5005
- **Files Modified**: `server/SecureConnectionManager.java`, `server.keystore`
- **Key Technologies**:
  - javax.net.ssl.SSLServerSocket for secure connections
  - SSLContext for security context configuration
  - KeyStore (JKS) for certificate management
  - KeyManagerFactory and TrustManagerFactory for key/trust management
  - Self-signed certificate generation with keytool
- **Description**: Added comprehensive SSL/TLS support to encrypt client-server communications. The implementation includes automatic keystore creation with self-signed certificates, configuration of strong cipher suites (AES-256-GCM, AES-128-GCM), and proper SSL session management. Demonstrates data confidentiality, integrity, and authentication in network programming.

### Member 5: [Name] - Client-Server Communication (Sockets)

- **Feature**: TCP socket and WebSocket communication infrastructure
- **Network Concept**: Socket programming for reliable, connection-oriented communication and WebSocket for real-time web updates
- **Ports**: 5000 (TCP), 5001 (WebSocket), 8080 (HTTP)
- **Files Modified**: `server/Server.java`, `client/Client.java`, `shared/Protocol.java`, `client/dashboard.html`
- **Key Technologies**:
  - java.net.ServerSocket and Socket for TCP connections
  - DataInputStream/DataOutputStream for stream-based I/O
  - org.java_websocket library for WebSocket server
  - com.sun.net.httpserver for HTTP file serving
  - Custom protocol design for message formatting
- **Description**: Established the complete client-server communication infrastructure. Implemented TCP socket server for console clients with reliable bidirectional communication. Added WebSocket server for real-time web client updates, enabling instant push notifications without polling. Included HTTP server to serve the web interface. Designed and implemented Protocol.java for standardized message formatting across all communication channels. Created console client (Client.java) demonstrating command-line auction interactions.

## Development Guidelines

- Each member should focus on their assigned feature and network concept
- Regular code reviews and integration testing required
- Document all network programming decisions and implementations
- Ensure compatibility between different network components
- Test for scalability and performance of network operations
