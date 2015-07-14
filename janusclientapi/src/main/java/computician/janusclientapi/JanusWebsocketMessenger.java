package computician.janusclientapi;

import android.util.Log;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocketHandshakeException;
import com.koushikdutta.async.http.WebSocketImpl;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusWebsocketMessenger implements IJanusMessenger {

    private final String uri;
    private final IJanusMessageObserver handler;
    private final JanusMessengerType type = JanusMessengerType.websocket;
    private WebSocket client = null;
    public JanusWebsocketMessenger(String uri, IJanusMessageObserver handler) throws URISyntaxException {
        this.uri = uri;
        this.handler = handler;
    }

    @Override
    public JanusMessengerType getMessengerType() { return type; }

    public void connect()
    {
        AsyncHttpClient.getDefaultInstance().websocket(uri, "janus-protocol", new AsyncHttpClient.WebSocketConnectCallback(){
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if(ex != null) {
                    handler.onError(ex);
                }
                client = webSocket;
                client.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        onMessage(s);
                    }
                });
                client.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) {
                            onError(ex);
                        } else {
                            onClose(-1, "unknown", true);
                        }
                    }
                });
                handler.onOpen();
            }
        });
    }

    public void onMessage(String message) {
        Log.w("onMessage", message);
        receivedMessage(message);
    }

    public void onClose(int code, String reason, boolean remote) {
        handler.onClose();
    }

    public void onError(Exception ex) {
        handler.onError(ex);
    }

    @Override
    public void disconnect() {
        client.close();
    }

    @Override
    public void sendMessage(String message) {
        Log.w("sendMessage", message);
        client.send(message);
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
