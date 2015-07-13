package computician.janusclient;

import computician.janusclient.util.SystemUiHider;
import computician.janusclientapi.*;


import android.annotation.TargetApi;
import android.app.Activity;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Printer;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.Console;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class JanusActivity extends Activity {
    private static final boolean AUTO_HIDE = true;
    private final String JANUS_URI = "ws://192.168.1.197:8188/janus";
    private JanusPluginHandle handle = null;
    private static int LOCAL_X = 0;
    private static int LOCAL_Y = 0;
    private static int LOCAL_WIDTH = 100;
    private static int LOCAL_HEIGHT = 100;
    private static int REMOTE_X = 0;
    private static int REMOTE_Y = 0;
    private static int REMOTE_WIDTH = 100;
    private static int REMOTE_HEIGHT = 100;


    //TODO Define these functions
    public class JanusGlobalCallbacks implements IJanusGatewayCallbacks {

        @Override
        public void onSuccess()
        {
            janusServer.Attach(new JanusPluginCallbacks());
        }

        @Override
        public void onDestroy() {}

        @Override
        public String getServerUri()
        {
            return JANUS_URI;
        }

        @Override
        public List<PeerConnection.IceServer> getIceServers()
        {
            ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
            iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
            return iceServers;
        }

        @Override
        public Boolean getIpv6Support() {
            return Boolean.FALSE;
        }

        @Override
        public Integer getMaxPollEvents() {
            return 0;
        }

        @Override
        public void onCallbackError(String error)
        {

        }
    }

    public class JanusPluginCallbacks implements IJanusPluginCallbacks {

        @Override
        public void success(JanusPluginHandle pluginHandle)
        {
            JanusActivity.this.handle = pluginHandle;
            JSONObject obj = new JSONObject();
            try
            {
                obj.put("audio", true);
                obj.put("video", true);
                handle.sendMessage(new IPluginHandleSendMessageCallbacks() {
                    @Override
                    public void onSuccessSynchronous(JSONObject obj) {
                        return;
                    }

                    @Override
                    public void onSuccesAsynchronous() {
                        return;
                    }

                    @Override
                    public JSONObject getJsep() {
                        return null;
                    }

                    @Override
                    public JSONObject getMessage() {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("audio", true);
                            obj.put("video", true);
                        } catch (Exception ex) {

                        }
                        return obj;
                    }

                    @Override
                    public void onCallbackError(String error) {
                        return;
                    }
                });
                handle.createOffer(new IPluginHandleWebRTCCallbacks() {
                    private JSONObject jsep = null;
                    private JSONObject msg = new JSONObject();

                    @Override
                    public JSONObject getJsep() {
                        return null;
                    }

                    @Override
                    public void onCallbackError(String error) {

                    }

                    @Override
                    public Boolean getTrickle() {
                        return true;
                    }

                    @Override
                    public JanusMediaConstraints getMedia() {
                        return new JanusMediaConstraints();
                    }

                    @Override
                    public void onSuccess(JSONObject obj) {
                        jsep = obj;
                        try
                        {
                            msg.put("audio", true);
                            msg.put("video", true);
                            handle.sendMessage(new IPluginHandleSendMessageCallbacks() {
                                @Override
                                public void onSuccessSynchronous(JSONObject obj) {

                                }

                                @Override
                                public void onSuccesAsynchronous() {

                                }

                                @Override
                                public JSONObject getJsep() {
                                    return jsep;
                                }

                                @Override
                                public JSONObject getMessage() {
                                    return msg;
                                }

                                @Override
                                public void onCallbackError(String error) {

                                }
                            });
                        }catch(Exception ex)
                        {

                        }
                    }
                });

            }catch(JSONException ex)
            {

            }
        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsep)
        {

        }

        @Override
        public void onLocalStream(MediaStream stream)
        {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        }

        @Override
        public void onRemoteStream(MediaStream stream)
        {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        }

        @Override
        public void onDataOpen(Object data)
        {

        }

        @Override
        public void onData(Object data)
        {

        }

        @Override
        public void onCleanup()
        {

        }

        @Override
        public JanusSupportedPluginPackages getPlugin()
        {
            return JanusSupportedPluginPackages.JANUS_ECHO_TEST;
        }

        @Override
        public void onCallbackError(String error)
        {

        }

        @Override
        public void onDetached()
        {

        }

    }

    public class JanusPluginWebRtcCallbacks implements IPluginHandleWebRTCCallbacks {
        @Override
        public void onSuccess(JSONObject obj){

        }

        @Override
        public JSONObject getJsep(){
            return null;
        }

        @Override
        public JanusMediaConstraints getMedia()
        {
            return new JanusMediaConstraints();
        }

        @Override
        public Boolean getTrickle() {
            return true;
        }

        @Override
        public void onCallbackError(String error) {

        }
    }

    public class JanusPluginHandleCallbacks implements IPluginHandleSendMessageCallbacks {

        @Override
        public void onCallbackError(String error) {

        }

        @Override
        public JSONObject getJsep() {
            return null;
        }

        @Override
        public JSONObject getMessage() {
            return null;
        }

        @Override
        public void onSuccesAsynchronous() {

        }

        @Override
        public void onSuccessSynchronous(JSONObject obj) {

        }
    }


    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private JanusServer janusServer;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    public class MyInit implements Runnable{
        public void run()
        {
            init();
        }
    }

    private void init()
    {
        try {
            janusServer = new JanusServer(new JanusGlobalCallbacks());
            EGLContext con = VideoRendererGui.getEGLContext();
            janusServer.initializeMediaContext(this, true, true, true, con);
            janusServer.Connect();
        }
        catch(Exception ex)
        {
            Log.w("computician.janusclient", ex.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_janus);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        vsv = (GLSurfaceView) findViewById(R.id.glview);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new MyInit());

        localRender = VideoRendererGui.create(70, 5, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
        remoteRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
    }
}
