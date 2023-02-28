package org.nnsama.douyu;

import lombok.Data;

import java.util.*;
import java.util.concurrent.BlockingQueue;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

@Data
public class MessageConsumer extends Thread {
    private volatile boolean needStop = false;
    //定义一个全局变量BlockingQueue
    private BlockingQueue<byte[]> msgQueue;
    private HashMap<String, List<MessageHandler>> handlers = new HashMap<>();

    public MessageConsumer(BlockingQueue<byte[]> msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void addHandler(String msgType, MessageHandler handler) {
        if (!handlers.containsKey(msgType)) {
            handlers.put(msgType, new ArrayList<>());
        }
        handlers.get(msgType).add(handler);
    }

    public void setStop(boolean needStop) {
        this.needStop = needStop;
    }

    @Override
    public void run() {
        while (!needStop) {
            try {
                byte[] data = msgQueue.take();
                String oriStr = PacketUtil.extractStrFromData(data);
                Map<String, String> msg = PacketUtil.parseStrToMap(oriStr);
                try {
                    String msgType = (String) msg.get("type");
                    if (handlers.containsKey(msgType)) {
                        for (MessageHandler handler : handlers.get(msgType)) {
                            handler.handle(msg);
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(MessageConsumer.class.getName()).warning("Invalid msg received. Exception: " + e.getMessage());
                }
                synchronized(msgQueue) {
                    while (!msgQueue.isEmpty()) {
                        msgQueue.remove();

                    }};
//                msgQueue.remove();
            } catch (InterruptedException e) {
                Logger.getLogger(MessageConsumer.class.getName()).warning("Thread interrupted. Exception: " + e.getMessage());
            }
        }
    }


}
