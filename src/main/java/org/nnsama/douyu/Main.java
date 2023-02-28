package org.nnsama.douyu;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class Main {

    private static final String HOST = "danmuproxy.douyu.com";
    private static final int PORT = 8601;

    private static final int ROOM_ID = 208114;

    private static final int HEARTBEAT_INTERVAL = 45;

    //定义一个hashmap常量
    private static final Map<String, String> otypeToStr = new HashMap<String, String>() {{
        put("0", "普通用户");
        put("1", "房管");
        put("2", "主播");
        put("3", "超管");
    }};


    public static void main(String[] args) {
        Client c = new Client(208114,HEARTBEAT_INTERVAL,HOST,PORT);
        c.addHandler("chatmsg", msg -> {
            try {
                String nn = (String) msg.get("nn");
                String txt = (String) msg.get("txt");
                String output = String.format("[%s] %s: %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), nn, txt);
                System.out.println(output);
                System.out.flush();
            } catch (Exception e) {
                System.err.printf("chatmsg_handler failed. Exception: %s%n", e);
            }
        });
        c.addHandler("uenter", msg -> {
            try {
                String nn = (String) msg.get("nn");
                String output = String.format("[%s] %s 进入了直播间",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), nn);
                System.out.println(output);
                System.out.flush();
            } catch (Exception e) {
                System.err.printf("uenter_handler failed. Exception: %s%n", e);
            }
        });
        c.addHandler("newblackres", msg -> {
            try {
                String otype = (String) msg.get("otype");
                String snic = (String) msg.get("snic");
                String dnic = (String) msg.get("dnic");
                long endTime = Long.parseLong( msg.get("endtime"));
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTime), ZoneId.systemDefault());
                String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String output = String.format("[%s] [%s] %s 封禁了 %s 到 %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        otypeToStr.getOrDefault(otype, "未知用户类型"), snic, dnic, timeStr);
                System.out.println(output);
                System.out.flush();
            } catch (Exception e) {
                System.err.printf("newblackres_handler failed. Exception: %s%n", e);
            }
        });
        c.start();
    }

}

