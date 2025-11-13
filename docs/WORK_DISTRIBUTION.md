# Work Distribution for Real-Time Auction System

## Group Members and Contributions

### Member 1: [Name] - Dashboard and Multi-Bidding Multiple Auction Handling

- **Feature**: Web dashboard with real-time multi-bidding and multiple auction management
- **Network Concept**: WebSocket for real-time updates and HTTP for web interface
- **Ports**: 8080 (HTTP), 5001 (WebSocket)
- **Files Modified**: `client/dashboard.html`, `client/styles.css`, `server/Server.java` (WebSocket implementation)
- **Key Technologies**:
  - WebSocket (java-websocket library) for bidirectional communication
  - HTML5 and CSS3 for responsive dashboard
  - JavaScript for real-time updates
  - com.sun.net.httpserver for HTTP serving
- **Description**: Implemented comprehensive web dashboard supporting multiple concurrent auctions with real-time bidding. Features include live bid updates, auction status monitoring, multi-auction handling, and responsive user interface. Demonstrates WebSocket efficiency for push-based notifications and HTTP for static content delivery.

### Member 2: [Name] - Notification and Login Multi-threading UDP

- **Feature**: UDP notification service with multi-threaded login system
- **Network Concept**: UDP broadcasting combined with multi-threading for concurrent user authentication
- **Port**: 5003
- **Files Modified**: `server/UDPNotificationService.java`, `server/Server.java`, login components
- **Key Technologies**:
  - DatagramSocket and DatagramPacket for UDP communication
  - Multi-threading for concurrent login processing
  - Broadcast addressing for notifications
  - Thread-safe session management
- **Description**: Developed UDP-based notification system with integrated multi-threaded login functionality. Implements subscription-based broadcasting for auction events while handling concurrent user authentication. Features include real-time notifications, secure login processing, and thread-safe user session management.

### Member 3: [Name] - Auction Creation Using Image

- **Feature**: Auction creation system with image upload and processing
- **Network Concept**: FTP protocol for file transfer integrated with TCP for auction metadata
- **Files Modified**: `server/ftp/FTPServer.java`, `server/auction/AuctionManager.java`, image processing components
- **Key Technologies**:
  - FTP server implementation for image uploads
  - Image validation and processing
  - File I/O and network file transfer
  - Auction data integration with images
- **Description**: Implemented auction creation functionality with image support. Includes FTP server for secure image uploads, image validation and processing, and integration with auction management system. Demonstrates file transfer protocols and multimedia handling in network applications.

### Member 4: [Name] - Chat System

- **Feature**: Real-time chat functionality for auction participants
- **Network Concept**: Java NIO for scalable non-blocking chat server
- **Port**: 5002
- **Files Modified**: `server/chat/ChatManager.java`, `server/Server.java`
- **Key Technologies**:
  - java.nio.channels.Selector for event-driven I/O
  - ServerSocketChannel and SocketChannel for non-blocking connections
  - ByteBuffer for efficient data handling
  - ConcurrentHashMap for thread-safe user management
- **Description**: Built scalable chat system using Java NIO for efficient handling of multiple concurrent chat connections. Features private messaging, user presence tracking, and broadcast messaging. Demonstrates non-blocking I/O patterns and event-driven architecture for high-performance network applications.

### Member 5: [Name] - Auction Summary and Activity

- **Feature**: Auction activity monitoring and summary reporting system
- **Network Concept**: TCP socket communication for data retrieval and real-time reporting
- **Port**: 5000
- **Files Modified**: `server/core/Server.java`, `server/auction/AuctionManager.java`, reporting components
- **Key Technologies**:
  - TCP sockets for reliable data communication
  - Data aggregation and analysis
  - Real-time activity logging
  - Report generation and formatting
- **Description**: Developed comprehensive auction monitoring system providing real-time activity tracking and summary reports. Includes data aggregation, performance metrics, and activity visualization. Demonstrates TCP reliability for critical data operations and real-time monitoring capabilities.

## Development Guidelines

- Each member should focus on their assigned feature and network concept
- Regular code reviews and integration testing required
- Document all network programming decisions and implementations
- Ensure compatibility between different network components
- Test for scalability and performance of network operations
