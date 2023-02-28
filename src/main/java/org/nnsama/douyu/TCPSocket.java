package org.nnsama.douyu;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.logging.Logger;
public class TCPSocket {
    private String host;
    private int port;
    private Socket socket;
    private boolean closed;
    public TCPSocket(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = new Socket();
        this.closed = true;
    }

    public void send(ByteArrayOutputStream data) {
        if (this.closed) {
            return;
        }
        try {
            OutputStream outputStream = this.socket.getOutputStream();
            outputStream.write(data.toByteArray());
            outputStream.flush();
        } catch (IOException e) {
            this.close();
            Logger.getLogger(TCPSocket.class.getName())
                    .warning("Socket send failed. Exception: " + e.getMessage());
        }
    }

    public void close() {
        if (!this.closed) {
            try {
                this.socket.close();
            } catch (IOException e) {
                Logger.getLogger(TCPSocket.class.getName())
                        .warning("Socket close failed. Exception: " + e.getMessage());
            }
            this.closed = true;
        }
    }

    public void connect() {
        if (this.closed) {
            while (true) {
                try {
                    this.socket = new Socket(this.host, this.port);
                } catch (IOException e) {
                    Logger.getLogger(TCPSocket.class.getName())
                            .warning(String.format("Socket connect failed with %s:%d. Exception: %s",
                                    this.host, this.port, e.getMessage()));
                    this.close();
                    this.socket = new Socket();
                    Logger.getLogger(TCPSocket.class.getName()).warning("Try reconnect in 5 seconds");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                break;
            }
            this.closed = false;
        }
    }

    public byte[] receive(int targetSize) {
        byte[] data = new byte[targetSize];
        int bytesRead = 0;
        while (bytesRead < targetSize) {
            try {
                int count = this.socket.getInputStream().read(data, bytesRead, targetSize - bytesRead);
                if (count == -1) {
                    this.close();
                    return null;
                }
                bytesRead += count;
            } catch (IOException e) {
                this.close();
                Logger.getLogger(TCPSocket.class.getName())
                        .warning("Socket receive failed. Exception: " + e.getMessage());
                return null;
            }
        }
        return data;
    }

}
