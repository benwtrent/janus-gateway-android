package computician.janusclientapi;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.*;
import java.math.BigInteger;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusPluginHandle implements IJanusWebRtcObserver{

    private class WebRtcStuff
    {
        public boolean started = false;
        public MediaStream myStream = null;
        public SessionDescription mySdp = null;
        public PeerConnection pc = null;
        public DataChannel dataChannel = null;
        public boolean trickle = true;
        public boolean iceDone = false;
        public boolean sdpSend = false;
    }

    private class WebRtcSDPObserver implements SdpObserver
    {
        private final IPluginHandleWebRTCCallbacks callbacks;
        private final IJanusWebRtcObserver handle;
        public WebRtcSDPObserver(IPluginHandleWebRTCCallbacks callbacks, IJanusWebRtcObserver handle)
        {
            this.callbacks = callbacks;
            this.handle = handle;
        }

        @Override
        public void onSetSuccess()
        {
            //todo JS api does not account for this
        }

        @Override
        public void onSetFailure(String error)
        {
            //todo JS api does not account for this
            callbacks.onCallbackError(error);
        }

        @Override
        public void onCreateSuccess(SessionDescription sdp)
        {
            handle.onLocalSdp(sdp, callbacks);
        }

        @Override
        public void onCreateFailure(String error)
        {
            callbacks.onCallbackError(error);
        }
    }

    private WebRtcStuff webrtcStuff;
    private PeerConnectionFactory sessionFactory = null;
    private final JanusServer server;
    public final JanusSupportedPluginPackages plugin;
    public final BigInteger id;
    private final IJanusPluginCallbacks callbacks;
    public JanusPluginHandle(JanusServer server, JanusSupportedPluginPackages plugin, BigInteger handle_id, IJanusPluginCallbacks callbacks)
    {
        this.server = server;
        this.plugin = plugin;
        id = handle_id;
        this.callbacks = callbacks;
        webrtcStuff = new WebRtcStuff();
    }

    public void consentDialog(boolean on)
    {
        callbacks.consentDialog(on);
    }

    public boolean initializeMediaContext(Context context, boolean audio, boolean video, boolean videoHwAcceleration, EGLContext eglContext)
    {
        if(!PeerConnectionFactory.initializeAndroidGlobals(context, audio, video, videoHwAcceleration, eglContext))
            return false;
        sessionFactory = new PeerConnectionFactory();
        return true;
    }

    public void onMessage(String msg)
    {

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
        server.sendMessage(TransactionType.plugin_handle_message, id, obj, plugin);
    }

    private void streamsDone(SessionDescription jsep, JanusMediaConstraints media, IPluginHandleWebRTCCallbacks webRTCCallbacks, MediaStream stream)
    {
        if(sessionFactory == null) {
            webRTCCallbacks.onCallbackError("WebRtc PeerFactory is not initialized. Please call initializeMediaContext");
            return;
        }
    }

    public void createOffer(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {
        //TODO create offer and peerconnection
        if(sessionFactory == null) {
            webrtcCallbacks.onCallbackError("WebRtc PeerFactory is not initialized. Please call initializeMediaContext");
            return;
        }
    }

    public void createAnswer(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {
        if(sessionFactory == null) {
            webrtcCallbacks.onCallbackError("WebRtc PeerFactory is not initialized. Please call initializeMediaContext");
            return;
        }
    }

    public void handleRemoteJsep(IPluginHandleWebRTCCallbacks webrtcCallbacks)
    {
        if(sessionFactory == null) {
            webrtcCallbacks.onCallbackError("WebRtc PeerFactory is not initialized. Please call initializeMediaContext");
            return;
        }
        JSONObject jsep = webrtcCallbacks.getJsep();
        if(jsep != null)
        {
            if(webrtcStuff.pc == null)
            {
                callbacks.error("No peerconnection created, if this is an answer please use createAnswer");
                return;
            }
            try
            {
                String sdpString = jsep.getString("sdp");
                SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(jsep.getString("type"));
                SessionDescription sdp = new SessionDescription(type, sdpString);
                webrtcStuff.pc.setRemoteDescription(new WebRtcSDPObserver(webrtcCallbacks, this), sdp);
            }
            catch (JSONException ex)
            {
                webrtcCallbacks.onCallbackError(ex.getMessage());
            }
        }
    }

    public void hangUp()
    {

    }

    public void detach()
    {

    }

    @Override
    public void onLocalSdp(SessionDescription sdp, IPluginHandleWebRTCCallbacks callbacks)
    {
        if(webrtcStuff.pc != null)
        {
            if(webrtcStuff.mySdp == null)
                webrtcStuff.mySdp = sdp;
            if(!webrtcStuff.iceDone && !webrtcStuff.trickle)
                return;
            if(webrtcStuff.sdpSend)
                return;

            webrtcStuff.pc.setLocalDescription(new WebRtcSDPObserver(callbacks, this), sdp);
        }
    }


}
