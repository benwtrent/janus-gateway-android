package computician.janusclientapi;

import java.math.BigInteger;

/**
 * Created by ben.trent on 5/7/2015.
 */
public interface IJanusMessenger {
    void connect();
    void disconnect();
    void sendMessage(String message);
    void sendMessage(String message, BigInteger session_id);
    void sendMessage(String message, BigInteger session_id, BigInteger handle_id);
    void receivedMessage(String message);
    JanusMessengerType getMessengerType();
}
