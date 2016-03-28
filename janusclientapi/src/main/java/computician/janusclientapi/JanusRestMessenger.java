package computician.janusclientapi;

import android.net.Uri;
import android.util.Log;

import java.math.BigInteger;
import com.koushikdutta.async.*;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.*;
import com.koushikdutta.async.http.body.*;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by ben.trent on 5/7/2015.
 */

//TODO big todo...it would be good to use androidasync as we already utilize that for the websocket endpoint
public class JanusRestMessenger implements IJanusMessenger {

    private final IJanusMessageObserver handler;
    private final String uri;
    private BigInteger session_id;
    private BigInteger handle_id;
    private String resturi;
    private final JanusMessengerType type = JanusMessengerType.restful;

    public void longPoll()
    {
        if(resturi.isEmpty())
            resturi = uri;


        AsyncHttpGet get = new AsyncHttpGet(uri+"/"+session_id.toString()+"&maxev=1");

        AsyncHttpClient.getDefaultInstance().executeJSONObject(get, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null)
                    receivedMessage(result.toString());
                else
                    handler.onError(e);
            }
        });
    }

    public JanusRestMessenger(String uri, IJanusMessageObserver handler) {
        this.handler = handler;
        this.uri = uri;
        resturi = "";
    }

    @Override
    public JanusMessengerType getMessengerType() {
        return type;
    }

    @Override
    public void connect() {
         AsyncHttpClient.getDefaultInstance().execute(uri, new HttpConnectCallback() {
             @Override
             public void onConnectCompleted(Exception ex, AsyncHttpResponse response) {
                 if(ex==null)
                    handler.onOpen();
                 else
                     handler.onError(new Exception("Failed to connect"));
             }
         });

        //todo
    }

    @Override
    public void disconnect() {

        //todo
    }

    @Override
    public void sendMessage(String message) {
        //todo
        Log.d("message", "Sent: \n\t" + message);
        if(resturi.isEmpty())
            resturi = uri;
        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(resturi),"post");
       AsyncHttpPost post = new AsyncHttpPost(resturi);

        JSONObject obj = null;
        try {
            obj = new JSONObject(message);
        }
        catch (Exception e)
        {

        }

        post.setBody(new JSONObjectBody(obj));

        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
               if(e==null)
                receivedMessage(result.toString());
                else
                   handler.onError(e);
            }
        });


    }

    @Override
    public void sendMessage(String message, BigInteger session_id) {
        //todo
        this.session_id = session_id;
        resturi = "";
        resturi = uri +"/"+ session_id.toString();
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message, BigInteger session_id, BigInteger handle_id) {
        //todo
        this.session_id = session_id;
        this.handle_id = handle_id;
        resturi = "";
        resturi = uri +"/"+ session_id.toString()+"/"+ handle_id.toString();
        sendMessage(message);
    }

    //todo
    private void handleNewMessage(String message) {

    }

    @Override
    public void receivedMessage(String msg) {

        try {
            Log.d("message", "Recv: \n\t" + msg);
            JSONObject obj = new JSONObject(msg);
            handler.receivedNewMessage(obj);
        } catch (Exception ex) {
            handler.onError(ex);
        }
    }
}
