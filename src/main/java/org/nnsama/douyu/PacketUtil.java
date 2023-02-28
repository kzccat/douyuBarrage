package org.nnsama.douyu;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class PacketUtil {
    private static final int CLIENT_MSG_TYPE = 689;
    private static final int RESERVED_DATA_FIELD = 0;


    public static String assembleLoginStr(int roomId) {
        String res = "type@=loginreq/roomid@=" + roomId + "/";
        return res;
    }

    public static String assembleJoinGroupStr(int roomId) {
        String res = "type@=joingroup/rid@=" + roomId + "/gid@=-9999/";
        return res;
    }

    public static String assembleHeartbeatStr() {
        String res = "type@=keeplive/tick@=" + System.currentTimeMillis() / 1000 + "/";
        return res;
    }

    public static String extractStrFromData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int packetSize = buffer.getInt();
        if (packetSize != data.length) {
            return "";
        }
        return new String(data, 8, packetSize - 9);
    }

    public static HashMap<String, String> parseStrToMap(String oriStr) {
        HashMap<String, String> res = new HashMap<>();
        String[] oriStrs = oriStr.split("/");
        for (String s : oriStrs) {
            String[] kv = s.split("@=");
            if (kv.length == 2) {
                res.put(kv[0], kv[1]);
            }
        }
        return res;
    }


    public static ByteArrayOutputStream send(String content) {
        try {
            //计算消息长度 = 消息长度(4) + 消息类型(4) + 真实消息内容长度 + 结尾标识长度(1)
            int contenLeng = 4 + 4 + content.length() + 1;
            //小端模式转换init (长度1)
            byte[] contenLeng1 = intToBytesLittle(contenLeng);
            //小端模式转换init (长度2)
            byte[] contenLeng2 = intToBytesLittle(contenLeng);
            //小端模式转换init (消息类型) (689:客户端发送给弹幕服务器的文本格式数据)
            byte[] msgType = intToBytesLittle(CLIENT_MSG_TYPE);
            //标识数据结尾
            int end = RESERVED_DATA_FIELD;

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            //写入长度1
            byteArray.write(contenLeng1);
            //写入长度2（与长度1相同）
            byteArray.write(contenLeng2);
            //写入消息类型
            byteArray.write(msgType);
            //写入消息内容
            byteArray.write(content.getBytes("ISO-8859-1"));
            //写入数据结尾标识
            byteArray.write(end);
            return byteArray;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
//
//    /**
//     * 读取消息
//     *
//     * @return
//     */
//    public static byte[] read(Socket socket) throws IOException {
//
//            InputStream inputStream = socket.getInputStream();
//            //下条信息的长度
//            int contentLen = 0;
//
//            //读取前4个字节，得到数据长度
//            byte[] bytes1 = readStream(inputStream,0,4);
//            contentLen = bytesToIntLittle(bytes1, 0); //用小端模式转换byte数组为
//            //System.out.println("数据长度1:" + contentLen);
//
//            //继续读取4个字节，得到第二个 数据长度
//            byte[] bytes2 = readStream(inputStream,0,4);
//            int contentLen2 = bytesToIntLittle(bytes2, 0);
//            //System.out.println("数据长度2:" + contentLen2);
//
//            //再次读取4个字节，得到消息类型
//            byte[] bytes3 = readStream(inputStream,0,4);
//            //将小端整数转换为大端整数
//            int msgType = bytesToIntLittle(bytes3, 0);
//
//            //System.out.println("消息类型:" + msgType);
//
//            //
//            contentLen = contentLen - 8;
//            //继续读取真正的消息内容
//            int len = 0;        //本次读取数据长度
//            int readLen = 0;    //已读数据长度
//            byte[] bytes = new byte[contentLen];
//            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//            while ((len = inputStream.read(bytes, 0, contentLen - readLen)) != -1) {
//                byteArray.write(bytes, 0, len);
//                readLen += len;
//                if (readLen == contentLen) {
//                    break;
//                }
//            }
//
//            return byteArray.toByteArray();
//
//    }

//    /**
//     * 计算消息体长度
//     */
//    private static int calcMessageLength(String content) {
//        return 4 + 4 + (content == null ? 0 : content.length()) + 1;
//    }


    /**
     * 以小端模式将int转成byte[]
     *
     * @param value
     * @return
     */
    public static byte[] intToBytesLittle(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }


//    /**
//     * 以小端模式将byte[]转成int
//     */
//    public static int bytesToIntLittle(byte[] src, int offset) {
//        int value;
//        value = (int) ((src[offset] & 0xFF)
//                | ((src[offset + 1] & 0xFF) << 8)
//                | ((src[offset + 2] & 0xFF) << 16)
//                | ((src[offset + 3] & 0xFF) << 24));
//        return value;
//    }


    /**
     * 从流中读取数据
     * @param inputStream
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public static byte[] readStream(InputStream inputStream, int off, int len) throws IOException {
        byte[] bytes = new byte[len];
        inputStream.read(bytes, 0, 4);
        return bytes;
    }
}


