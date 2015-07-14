package computician.janusclientapi;

import android.util.Log;

import java.math.BigInteger;
import java.net.URISyntaxException;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

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
    public JanusMessengerType getMessengerType() {
        return type;
    }

    public void connect() {
        AsyncHttpClient.getDefaultInstance().websocket(uri, "janus-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
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
                        Log.d("JANUSCLIENT", "Socket closed for some reason");
                        if (ex != null) {
                            Log.d("JANUSCLIENT", "SOCKET EX " + ex.getMessage());
                        }
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
        Log.d("JANUSCLIENT", "Recv: \n\t" + message);
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
        Log.d("JANUSCLIENT", "Sent: \n\t" + message);
        client.send(message);
    }

    @Override
    public void sendMessage(String message, BigInteger session_id) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message, BigInteger session_id, BigInteger handle_id) {
        sendMessage(message);
    }

    @Override
    public void receivedMessage(String msg) {
        try {
            JSONObject obj = new JSONObject(msg);
            handler.receivedNewMessage(obj);
        } catch (JSONException ex) {
            handler.onError(ex);
        }
    }
}
