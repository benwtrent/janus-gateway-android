package computician.janusclientapi;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusWebsocketMessenger extends WebSocketClient implements IJanusMessenger {

    public static class ProtocolMap
    {
        public static Map<String, String> getProtocolMap(String protocol)
        {
            Map<String, String> map = new HashMap<>();
            map.put("0", protocol);
            return map;
        }
    }

    private final URI uri;
    private final IJanusMessageObserver handler;
    private final JanusMessengerType type = JanusMessengerType.websocket;
    public JanusWebsocketMessenger(String uri, Map<String, String> map, IJanusMessageObserver handler) throws URISyntaxException {
        super(new URI(uri), new Draft_10(), map, 250);
        this.uri = new URI(uri);
        this.handler = handler;
    }

    @Override
    public JanusMessengerType getMessengerType() { return type; }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        //todo
        handler.onOpen();
    }

    @Override
    public void onMessage(String message) {
        receivedMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        handler.onClose();
    }

    @Override
    public void onError(Exception ex) {
        handler.onError(ex);
    }

    @Override
    public void disconnect() {
        super.close();
    }

    @Override
    public void sendMessage(String message) {
        super.send(message);
    }

    @Override
    public void sendMessage(String message, BigInteger session_id)
    {
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message, BigInteger session_id, BigInteger handle_id)
    {
        sendMessage(message);
    }

    @Override
    public void receivedMessage(String msg){
        try {
            JSONObject obj = new JSONObject(msg);
            handler.receivedNewMessage(obj);
        }
        catch(JSONException ex)
        {
            handler.onError(ex);
        }
    }
}
