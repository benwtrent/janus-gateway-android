package computician.janusclientapi;

/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusIceServer {

    public final String uri;
    public final String username;
    public final String password;

    public JanusIceServer(String uri) { this(uri, "", ""); }

    public JanusIceServer(String uri, String username, String password)
    {
        this.uri = uri;
        this.password = password;
        this.username = username;
    }



}
