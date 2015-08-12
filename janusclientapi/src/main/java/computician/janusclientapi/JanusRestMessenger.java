package computician.janusclientapi;

import java.math.BigInteger;
import com.koushikdutta.async.*;
import com.koushikdutta.async.http.*;
import com.koushikdutta.async.http.body.*;


/**
 * Created by ben.trent on 5/7/2015.
 */

//TODO big todo...it would be good to use androidasync as we already utilize that for the websocket endpoint
public class JanusRestMessenger implements IJanusMessenger {

    private final IJanusMessageObserver handler;
    private final String uri;
    private final JanusMessengerType type = JanusMessengerType.restful;

    private void longPoll()
    {

    }

    public JanusRestMessenger(String uri, IJanusMessageObserver handler) {
        this.handler = handler;
        this.uri = uri;
    }

    @Override
    public JanusMessengerType getMessengerType() {
        return type;
    }

    @Override
    public void connect() {
        //todo
    }

    @Override
    public void disconnect() {
        //todo
    }

    @Override
    public void sendMessage(String message) {
        //todo
    }

    @Override
    public void sendMessage(String message, BigInteger session_id) {
        //todo
    }

    @Override
    public void sendMessage(String message, BigInteger session_id, BigInteger handle_id) {
        //todo
    }

    //todo
    private void handleNewMessage(String message) {

    }

    @Override
    public void receivedMessage(String msg) {
    }
}
