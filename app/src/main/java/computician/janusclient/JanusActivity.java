package computician.janusclient;

import computician.janusclient.util.SystemUiHider;
import computician.janusclientapi.*;

import android.app.Activity;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.List;

public class JanusActivity extends Activity {
    private static final boolean AUTO_HIDE = true;
    private final String JANUS_URI = "ws://192.168.1.197:8188";
    private JanusPluginHandle handle = null;

    //TODO Define these functions
    public class JanusGlobalCallbacks implements IJanusGatewayCallbacks {

        @Override
        public void onSuccess() {
            janusServer.Attach(new JanusPluginCallbacks());
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public String getServerUri() {
            return JANUS_URI;
        }

        @Override
        public List<PeerConnection.IceServer> getIceServers() {
            ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
            //iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
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
        public void onCallbackError(String error) {

        }
    }

    public class JanusPluginCallbacks implements IJanusPluginCallbacks {

        @Override
        public void success(JanusPluginHandle pluginHandle) {
            JanusActivity.this.handle = pluginHandle;
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
                    JSONObject msg = new JSONObject();
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("audio", true);
                        obj.put("video", true);
                        msg.put("message", obj);
                    } catch (Exception ex) {

                    }
                    return msg;
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
                    Log.d("JANUSCLIENT", "OnSuccess for CreateOffer called");
                    jsep = obj;
                    try {
                        JSONObject body = new JSONObject();
                        body.put("audio", true);
                        body.put("video", true);
                        msg.put("message", body);
                        msg.put("jsep", jsep);
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
                    } catch (Exception ex) {

                    }
                }
            });

        }

        @Override
        public void onMessage(JSONObject msg, final JSONObject jsepLocal) {
            if(jsepLocal != null)
            {
                handle.handleRemoteJsep(new IPluginHandleWebRTCCallbacks() {
                    JSONObject myJsep = jsepLocal;
                    @Override
                    public void onSuccess(JSONObject obj) {

                    }

                    @Override
                    public JSONObject getJsep() {
                        return myJsep;
                    }

                    @Override
                    public JanusMediaConstraints getMedia() {
                        return null;
                    }

                    @Override
                    public Boolean getTrickle() {
                        return null;
                    }

                    @Override
                    public void onCallbackError(String error) {

                    }
                });
            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
            VideoRendererGui.update(localRender, 0, 0, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
        }

        @Override
        public void onRemoteStream(MediaStream stream) {
            stream.videoTracks.get(0).setEnabled(true);
            if(stream.videoTracks.get(0).enabled())
                Log.d("JANUSCLIENT", "video tracks enabled");
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
            VideoRendererGui.update(remoteRender, 0, 0, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRendererGui.update(localRender, 72, 72, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
        }

        @Override
        public void onDataOpen(Object data) {

        }

        @Override
        public void onData(Object data) {

        }

        @Override
        public void onCleanup() {

        }

        @Override
        public JanusSupportedPluginPackages getPlugin() {
            return JanusSupportedPluginPackages.JANUS_ECHO_TEST;
        }

        @Override
        public void onCallbackError(String error) {

        }

        @Override
        public void onDetached() {

        }

    }

    public class JanusPluginWebRtcCallbacks implements IPluginHandleWebRTCCallbacks {
        @Override
        public void onSuccess(JSONObject obj) {

        }

        @Override
        public JSONObject getJsep() {
            return null;
        }

        @Override
        public JanusMediaConstraints getMedia() {
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

    public class MyInit implements Runnable {
        public void run() {
            init();
        }
    }

    private void init() {
        try {
            janusServer = new JanusServer(new JanusGlobalCallbacks());
            EGLContext con = VideoRendererGui.getEGLContext();
            janusServer.initializeMediaContext(this, true, true, true, con);
            janusServer.Connect();
        } catch (Exception ex) {
            Log.w("computician.janusclient", ex.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_janus);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        vsv = (GLSurfaceView) findViewById(R.id.glview);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new MyInit());

        localRender = VideoRendererGui.create(70, 5, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
        remoteRender = VideoRendererGui.create(0, 0, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
    }
}
