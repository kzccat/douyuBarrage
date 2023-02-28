package org.nnsama.douyu;

public class Client {
    private int room_id;
    private int heartbeat_interval;
    private String barrage_host;
    private int barrage_port;
    private TCPSocket tcp_socket;
    private MessageWorker message_worker;
    private HeartbeatWorker heartbeat_worker;

    public Client(int room_id, int heartbeat_interval, String barrage_host, int barrage_port) {
        this.room_id = room_id;
        this.heartbeat_interval = heartbeat_interval;
        this.barrage_host = barrage_host;
        this.barrage_port = barrage_port;
        this.tcp_socket = new TCPSocket(this.barrage_host, this.barrage_port);
        this.message_worker = new MessageWorker(this.tcp_socket, this.room_id);
        this.heartbeat_worker = new HeartbeatWorker(tcp_socket, this.heartbeat_interval);
    }

    public void addHandler(String msgType, MessageHandler handler) {
        this.message_worker.addHandler(msgType, handler);
    }

    public void setHeartbeatInterval(int interval) {
        this.heartbeat_interval = interval;
        this.heartbeat_worker.setHeartbeat_interval(interval);
    }

    public void refreshObject() {
        this.tcp_socket = new TCPSocket(this.barrage_host, this.barrage_port);
        this.message_worker = new MessageWorker(this.tcp_socket, this.room_id);
        this.heartbeat_worker = new HeartbeatWorker(this.tcp_socket, this.heartbeat_interval);
    }

    public void setRoomId(int room_id) {
        this.room_id = room_id;
        this.message_worker.setRoomId(room_id);
    }


    public void start() {
        this.tcp_socket.connect();
        this.message_worker.start();
        this.heartbeat_worker.start();
    }

    public void stop() {
        this.message_worker.setStop(true);
        this.heartbeat_worker.set_stop(true);
        this.tcp_socket.close();
        this.refreshObject();
    }
}