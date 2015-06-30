package computician.janusclientapi;

/**
 * Created by ben.trent on 5/7/2015.
 */
public interface IJanusMessenger {
    void connect();
    void disconnect();
    void sendMessage(String message);
    void receivedMessage(String message);
    JanusMessengerType getMessengerType();
}
