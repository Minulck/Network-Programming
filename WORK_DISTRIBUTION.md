# Work Distribution for Real-Time Auction System

## Group Members and Contributions

### Member 1: [Name] - Chat System with Java NIO

- **Feature**: Real-time private chat using Java NIO (Non-blocking I/O)
- **Network Concept**: Non-blocking I/O with Selector, SelectionKey, and SocketChannel
- **Files Modified**: `server/ChatManager.java`, `server/Server.java`, `client/dashboard.html`
- **Description**: Implemented a scalable chat system using Java NIO for efficient handling of multiple concurrent chat connections, with private messaging and user presence features.

### Member 2: [Name] - UDP Notification Service

- **Feature**: UDP-based notification broadcasting
- **Network Concept**: User Datagram Protocol for fast, connectionless notifications
- **Files Modified**: `server/UDPNotificationService.java`, `server/Server.java`
- **Description**: Implemented UDP broadcasting for instant notifications about auction events, showcasing lightweight messaging for non-critical updates.

### Member 3: [Name] - Multi-threading Server Architecture

- **Feature**: Multi-threaded client handling for concurrent users
- **Network Concept**: Multi-threading to handle multiple simultaneous client connections
- **Files Modified**: `server/ClientHandler.java`, `server/Server.java`
- **Description**: Implemented thread-per-client model to allow multiple users to connect and interact with the auction system simultaneously without blocking.

### Member 4: [Name] - Secure Connection Management

- **Feature**: SSL/TLS encryption for secure communications
- **Network Concept**: Secure Socket Layer/Transport Layer Security for encrypted network communication
- **Files Modified**: `server/SecureConnectionManager.java`, `server.keystore`
- **Description**: Added SSL/TLS support to encrypt client-server communications, ensuring data security and demonstrating secure network programming practices.

### Member 5: [Name] - Client-Server Communication (Sockets)

- **Feature**: Basic TCP socket communication infrastructure
- **Network Concept**: Socket programming for reliable, connection-oriented communication
- **Files Modified**: `server/Server.java`, `client/Client.java`, `shared/Protocol.java`
- **Description**: Established the foundation for client-server architecture using Java Sockets API, enabling bidirectional communication for auction commands and responses.

## Development Guidelines

- Each member should focus on their assigned feature and network concept
- Regular code reviews and integration testing required
- Document all network programming decisions and implementations
- Ensure compatibility between different network components
- Test for scalability and performance of network operations
