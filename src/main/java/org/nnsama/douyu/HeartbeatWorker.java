package org.nnsama.douyu;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

@Data
public class HeartbeatWorker extends Thread {
    private boolean need_stop;
    private final TCPSocket socket;
    private  int heartbeat_interval;

    public HeartbeatWorker(TCPSocket socket, int heartbeat_interval) {
        this.need_stop = false;
        this.socket = socket;
        this.heartbeat_interval = heartbeat_interval;
    }

    public void set_stop(boolean need_stop) {
        this.need_stop = need_stop;
    }

    @Override
    public void run() {
        while (!need_stop) {
//            String ori_str = PacketUtil.assembleHeartbeatStr();
//            byte[] data = PacketUtil.assembleTransferData(ori_str);
//            socket.send(ByteBuffer.allocate(4).putInt(data.length).array());
//            socket.send(data);
            String ori_str = PacketUtil.assembleHeartbeatStr();
            ByteArrayOutputStream send = PacketUtil.send(ori_str);
            socket.send(send);

            try {
                Thread.sleep(heartbeat_interval * 1000);
            } catch (InterruptedException e) {
                // handle exception
            }
        }
    }
}
