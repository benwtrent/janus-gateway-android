package computician.janusclientapi;

import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.SessionDescription;

import java.math.BigInteger;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusPluginHandle {
    private JanusWebRtcStuff webrtcStuff;
    private final JanusServer session;
    public final JanusSupportedPluginPackages plugin;
    public final BigInteger id;
    private final IJanusPluginCallbacks callbacks;
    public JanusPluginHandle(JanusServer server, JanusSupportedPluginPackages plugin, BigInteger handle_id, IJanusPluginCallbacks callbacks)
    {
        session = server;
        this.plugin = plugin;
        id = handle_id;
        this.callbacks = callbacks;
    }

    public void consentDialog(boolean on)
    {
        callbacks.consentDialog(on);
    }

    public void onMessage(JSONObject msg, JSONObject jsep)
    {
        callbacks.onMessage(msg, jsep);
    }

    public void onLocalStream(MediaStream stream)
    {
        callbacks.onLocalStream(stream);
    }

    public void onRemoteStream(MediaStream stream)
    {
        callbacks.onRemoteStream(stream);
    }

    public void onDataOpen(Object data)
    {
        callbacks.onDataOpen(data);
    }

    public void onData(Object data)
    {
        callbacks.onData(data);
    }

    public void onCleanup()
    {
        callbacks.onCleanup();
    }

    public void onDetached()
    {
        callbacks.onDetached();
    }

    public void sendMessage(IPluginHandleSendMessageCallbacks obj)
    {

    }

    public void createOffer(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {

    }

    public void createAnswer(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {

    }

    public void handleRemoteJsep(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {

    }

    public void hangUp()
    {

    }

    public void detach()
    {

    }

}
