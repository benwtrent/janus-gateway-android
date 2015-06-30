package computician.janusclientapi;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusRestMessenger implements IJanusMessenger {

    private final IJanusMessageObserver handler;
    private final URI uri;
    private final JanusMessengerType type = JanusMessengerType.restful;
    public JanusRestMessenger(String uri, IJanusMessageObserver handler) throws URISyntaxException {
        this.handler = handler;
        this.uri = new URI(uri);
    }

    @Override
    public JanusMessengerType getMessengerType() { return type; }

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

    //todo
    private void handleNewMessage(String message) {

    }

    @Override
    public void receivedMessage(String msg){}
}
