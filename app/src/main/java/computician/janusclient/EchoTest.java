package computician.janusclient;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.List;

import computician.janusclientapi.IJanusGatewayCallbacks;
import computician.janusclientapi.IJanusPluginCallbacks;
import computician.janusclientapi.IPluginHandleSendMessageCallbacks;
import computician.janusclientapi.IPluginHandleWebRTCCallbacks;
import computician.janusclientapi.JanusMediaConstraints;
import computician.janusclientapi.JanusPluginHandle;
import computician.janusclientapi.JanusServer;
import computician.janusclientapi.JanusSupportedPluginPackages;

/**
 * Created by ben.trent on 7/24/2015.
 */


public class EchoTest {

    private final String JANUS_URI = "ws://192.168.1.197:8188";
    private JanusPluginHandle handle = null;
    private VideoRenderer.Callbacks localRender, remoteRender;
    private JanusServer janusServer;

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
            EchoTest.this.handle = pluginHandle;
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

    public EchoTest(VideoRenderer.Callbacks localRender, VideoRenderer.Callbacks remoteRender) {
        this.localRender = localRender;
        this.remoteRender = remoteRender;
        janusServer = new JanusServer(new JanusGlobalCallbacks());
    }

    public boolean initializeMediaContext(Context context, boolean audio, boolean video, boolean videoHwAcceleration, EGLContext eglContext){
        return janusServer.initializeMediaContext(context, audio, video, videoHwAcceleration, eglContext);
    }

    public void Start() {
        janusServer.Connect();
    }
}
