# FTP Image Upload Feature for Real-Time Auction System

## Overview
This feature allows users to upload images when creating auctions using the FTP (File Transfer Protocol) network method. The images are then displayed in the active auctions list.

## Architecture

### Components Added:

1. **FTPServer.java** (`server/FTPServer.java`)
   - Standalone FTP server running on port 5010
   - Handles file upload requests using the FTP protocol
   - Stores uploaded files in the `uploads/` directory
   - Generates unique filenames to prevent conflicts (timestamp_originalname)
   - Supports concurrent uploads using thread pool

2. **HTTP Bridge** (`server/Server.java`)
   - HTTP endpoint `/ftp-upload` that acts as a bridge between browser and FTP server
   - Accepts multipart/form-data from the browser
   - Forwards the file to the FTP server using TCP socket connection
   - Returns the uploaded filename to the client

3. **Updated Auction Model** (`server/Auction.java`)
   - Added `imageName` field to store the uploaded image filename
   - Added getter/setter methods for image name
   - Modified constructor to accept optional image name

4. **Updated Protocol** (`shared/Protocol.java`)
   - Enhanced `newAuctionMessage()` to include image name parameter
   - Maintains backward compatibility with old message format

5. **Client Interface** (`client/dashboard.html`)
   - Added image file input in the create auction form
   - Image preview before upload
   - FTP upload function using HTTP bridge
   - Display uploaded images in auction cards

## How It Works

### Upload Process:
1. User selects an image file when creating an auction
2. JavaScript shows a preview of the selected image
3. On form submit:
   - Image is uploaded to HTTP endpoint `/ftp-upload` using FormData
   - Server receives the multipart data and extracts file content
   - Server connects to FTP server on port 5010 via TCP socket
   - Sends `UPLOAD|filename|filesize` command
   - FTP server responds with `OK|unique_filename`
   - Server sends the file data bytes
   - FTP server saves file and responds with `SUCCESS|unique_filename`
   - Server returns the unique filename to the client
4. Client creates auction with the image filename
5. Server broadcasts the auction with image information

### Display Process:
1. When auction is created, server includes image filename in broadcast
2. Client receives `NEW_AUCTION|id|name|price|duration|imageName`
3. Client creates auction card with `<img>` tag pointing to `/uploads/imagename`
4. HTTP server serves the image file from uploads directory

## FTP Protocol Implementation

### Commands:
- **UPLOAD|filename|filesize** - Initiates file upload
  - Response: `OK|unique_filename` or `ERROR|message`
- **LIST** - Lists all uploaded files (optional, not used by client)
  - Response: `FILES|file1,file2,file3,...`

### File Transfer:
1. Client sends UTF command string
2. Server responds with UTF acknowledgment
3. Client sends raw file bytes
4. Server saves to disk and sends success/error response

## Security Features

### Filename Sanitization:
- Removes special characters and path traversal attempts
- Replaces invalid characters with underscores
- Prepends timestamp to prevent name collisions

### File Storage:
- All files stored in isolated `uploads/` directory
- No user can access files outside this directory
- Server validates file path before serving

## Testing the Feature

### Steps to Test:
1. Start the server: `.\start-server.bat`
2. Open browser to `http://localhost:8080`
3. Login with any username
4. Navigate to "Create Auction" tab
5. Fill in auction details:
   - Name: "Test Item"
   - Starting Price: 100
   - Duration: 60
6. Click "Choose File" and select an image (JPG, PNG, GIF)
7. See image preview
8. Click "Launch Auction"
9. Image uploads via FTP
10. Auction appears in Active Auctions with the image displayed

### Expected Behavior:
- Image preview shows before upload
- Toast notification: "Uploading image via FTP..."
- Toast notification: "Image uploaded successfully!"
- Toast notification: "Auction created successfully!"
- Auction card displays with image at the top
- Other connected users see the auction with image

## File Locations

```
RealTimeAuctionSystem/
├── server/
│   ├── FTPServer.java           (NEW - FTP server implementation)
│   ├── Server.java               (MODIFIED - Added HTTP bridge endpoint)
│   ├── Auction.java              (MODIFIED - Added imageName field)
│   └── AuctionManager.java       (MODIFIED - Handles image parameter)
├── shared/
│   └── Protocol.java             (MODIFIED - Added image to protocol)
├── client/
│   └── dashboard.html            (MODIFIED - Added image upload UI)
├── uploads/                      (NEW - Image storage directory)
└── FTP_IMAGE_UPLOAD_GUIDE.md    (This file)
```

## Network Ports

- **5000** - Main auction server (TCP)
- **5001** - WebSocket server
- **5002** - Chat server (NIO)
- **5003** - UDP notifications
- **5005** - Secure SSL/TLS server
- **5010** - FTP server (NEW)
- **8080** - HTTP server (web interface + file serving)

## Protocol Message Format

### Creating Auction Without Image:
```
CREATE|AuctionName|100.00|60
```

### Creating Auction With Image:
```
CREATE|AuctionName|100.00|60|1234567890_image.jpg
```

### Broadcast New Auction:
```
NEW_AUCTION|0|AuctionName|100.00|60|1234567890_image.jpg
```

## Advantages of FTP Implementation

1. **Separation of Concerns**: File transfer is handled by dedicated FTP server
2. **Scalability**: FTP server can handle multiple concurrent uploads
3. **Standard Protocol**: Uses well-established FTP concepts
4. **Efficient**: Direct binary transfer without encoding overhead
5. **Reusability**: FTP server can be used by other clients/services

## Future Enhancements

1. Add file size validation (max 5MB)
2. Add image format validation (only JPG, PNG, GIF)
3. Add image compression before upload
4. Support multiple images per auction
5. Add image delete functionality
6. Implement thumbnail generation
7. Add FTP authentication
8. Support FTPS (FTP over SSL/TLS)

## Troubleshooting

### Image Not Uploading:
- Check FTP server is running (port 5010)
- Check uploads directory exists and is writable
- Check browser console for errors
- Verify file size is reasonable (<10MB)

### Image Not Displaying:
- Check image was uploaded to uploads/ directory
- Check HTTP server is serving /uploads/* paths
- Check image filename in auction message
- Try accessing image directly: `http://localhost:8080/uploads/filename.jpg`

### Connection Errors:
- Ensure all services started successfully
- Check no firewall is blocking ports
- Verify localhost resolution

## Conclusion

The FTP image upload feature successfully integrates file transfer capabilities into the auction system using the FTP network protocol. The implementation is modular, secure, and provides a good user experience with image previews and real-time uploads.
