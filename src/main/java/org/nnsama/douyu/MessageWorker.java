package org.nnsama.douyu;

import java.io.ByteArrayOutputStream;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class MessageWorker extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MessageWorker.class);

    private boolean needStop = false;
    private TCPSocket socket;
    private int roomId;
    private BlockingQueue<byte[]> msgQueue;
    private MessageConsumer messageConsumer;

    public MessageWorker(TCPSocket socket, int roomId) {
        this.socket = socket;
        this.roomId = roomId;
        this.msgQueue = new LinkedBlockingQueue<>();
        this.messageConsumer = new MessageConsumer(msgQueue);
    }

    public void addHandler(String msgType, MessageHandler handler) {
        this.messageConsumer.addHandler(msgType, handler);
    }

    public void setStop(boolean needStop) {
        this.needStop = needStop;
        this.messageConsumer.setStop(needStop);
    }

    public void prepare() {
        this.messageConsumer.start();
        enterRoom();
    }

    private void enterRoom() {
        String oriStr = PacketUtil.assembleLoginStr(this.roomId);
        ByteArrayOutputStream send = PacketUtil.send(oriStr);
        this.socket.send(send);
        oriStr = PacketUtil.assembleJoinGroupStr(this.roomId);
        send = PacketUtil.send(oriStr);
        this.socket.send(send);
    }

    @Override
    public void run() {
        prepare();
        while (!needStop) {
            byte[] packetSizeBytes = socket.receive(4);
            if (packetSizeBytes == null) {
                logger.warn("Socket closed");
                socket.connect();
                enterRoom();
                continue;
            }
            //返回由给定字节数组所表示的整数值


            int packetSize = bytesToInt(packetSizeBytes);
            byte[] data = socket.receive(packetSize);
            if (data == null) {
                logger.warn("Socket closed");
                socket.connect();
                enterRoom();
                continue;
            }
            try {
                msgQueue.put(data);
            } catch (InterruptedException e) {
                logger.error("Failed to put data into message queue", e);
            }
        }
    }

    public static int bytesToInt(byte[] a){
        int ans=0;
        for(int i=0;i<4;i++){
            ans<<=8;
            ans|=(a[3-i]&0xff);
            /* 这种写法会看起来更加清楚一些：
            int tmp=a[3-i];
            tmp=tmp&0x000000ff;
            ans|=tmp;*/
//            System.out.println();(ans);
        }
        return ans;
    }

}
