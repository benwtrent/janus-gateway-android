package computician.janusclient;

import computician.janusclient.util.SystemUiHider;
import computician.janusclientapi.*;


import android.annotation.TargetApi;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class JanusActivity extends Activity {
    private static final boolean AUTO_HIDE = true;
    private final String JANUS_URI = "ws://192.168.1.89:8188/janus";

    //TODO Define these functions
    public class JanusGlobalCallbacks implements IJanusGatewayCallbacks {

        @Override
        public void onSuccess() {}

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
        public void success(JanusPluginHandle handle)
        {

        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsep)
        {

        }

        @Override
        public void onLocalStream(MediaStream stream)
        {

        }

        @Override
        public void onRemoteStream(MediaStream stream)
        {

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
            return JanusSupportedPluginPackages.JANUS_VIDEO_ROOM;
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

    private void init()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_janus);


        vsv = (GLSurfaceView) findViewById(R.id.glview);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, ()->{ init();});
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
