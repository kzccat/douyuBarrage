package org.nnsama.douyu;

import java.util.Map;

public interface MessageHandler {
    void handle(Map<String,String> msg);
}
