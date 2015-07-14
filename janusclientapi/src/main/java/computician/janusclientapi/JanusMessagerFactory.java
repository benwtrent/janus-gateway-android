package computician.janusclientapi;

import java.net.URISyntaxException;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusMessagerFactory {
    public static IJanusMessenger createMessager(String uri, IJanusMessageObserver handler) throws URISyntaxException {

        if (uri.indexOf("ws") == 0) {
            return new JanusWebsocketMessenger(uri, handler);
        } else {
            return new JanusRestMessenger(uri, handler);
        }
    }
}
