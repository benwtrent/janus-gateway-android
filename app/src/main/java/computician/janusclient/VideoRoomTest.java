package computician.janusclient;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import computician.janusclientapi.IJanusGatewayCallbacks;
import computician.janusclientapi.IJanusPluginCallbacks;
import computician.janusclientapi.IPluginHandleWebRTCCallbacks;
import computician.janusclientapi.JanusMediaConstraints;
import computician.janusclientapi.JanusPluginHandle;
import computician.janusclientapi.JanusServer;
import computician.janusclientapi.JanusSupportedPluginPackages;
import computician.janusclientapi.PluginHandleSendMessageCallbacks;
import computician.janusclientapi.PluginHandleWebRTCCallbacks;

//TODO create message classes unique to this plugin
/**
 * Created by ben.trent on 7/24/2015.
 */
public class VideoRoomTest {
    private final String JANUS_URI = "ws://192.168.1.197:8188";
    private JanusPluginHandle handle = null;
    private VideoRenderer.Callbacks localRender;
    private Stack<VideoRenderer.Callbacks> availableRemoteRenderers = new Stack<>();
    private HashMap<BigInteger, VideoRenderer.Callbacks> remoteRenderers = new HashMap<>();
    private JanusServer janusServer;
    private BigInteger myid;
    final private String user_name = "android";
    final private int roomid = 1234;

    class ListenerAttachCallbacks implements IJanusPluginCallbacks{
        final private VideoRenderer.Callbacks renderer;
        final private BigInteger feedid;
        private JanusPluginHandle listener_handle = null;

        public ListenerAttachCallbacks(BigInteger id, VideoRenderer.Callbacks renderer){
            this.renderer = renderer;
            this.feedid = id;
        }

        public void success(JanusPluginHandle handle) {
            listener_handle = handle;
            try
            {
                JSONObject body = new JSONObject();
                JSONObject msg = new JSONObject();
                body.put("request", "join");
                body.put("room", roomid);
                body.put("ptype", "listener");
                body.put("feed", feedid);
                msg.put("message", body);
                handle.sendMessage(new PluginHandleSendMessageCallbacks(msg));
            }
            catch(Exception ex)
            {

            }
        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsep) {

            try {
                String event = msg.getString("videoroom");
                if (event.equals("attached") && jsep != null) {
                    final JSONObject remoteJsep = jsep;
                    listener_handle.createAnswer(new IPluginHandleWebRTCCallbacks() {
                        @Override
                        public void onSuccess(JSONObject obj) {
                            try {
                                JSONObject mymsg = new JSONObject();
                                JSONObject body = new JSONObject();
                                body.put("request", "start");
                                body.put("room", roomid);
                                mymsg.put("message", body);
                                mymsg.put("jsep", obj);
                                listener_handle.sendMessage(new PluginHandleSendMessageCallbacks(mymsg));
                            } catch (Exception ex) {

                            }
                        }

                        @Override
                        public JSONObject getJsep() {
                            return remoteJsep;
                        }

                        @Override
                        public JanusMediaConstraints getMedia() {
                            JanusMediaConstraints cons = new JanusMediaConstraints();
                            cons.setVideo(null);
                            cons.setRecvAudio(true);
                            cons.setRecvVideo(true);
                            cons.setSendAudio(false);
                            return cons;
                        }

                        @Override
                        public Boolean getTrickle() {
                            return true;
                        }

                        @Override
                        public void onCallbackError(String error) {

                        }
                    });
                }
            }
            catch(Exception ex)
            {

            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {

        }

        @Override
        public void onRemoteStream(MediaStream stream) {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(renderer));
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
        public void onDetached() {

        }

        @Override
        public JanusSupportedPluginPackages getPlugin() {
            return JanusSupportedPluginPackages.JANUS_VIDEO_ROOM;
        }

        @Override
        public void onCallbackError(String error) {

        }
    }

    private void publishOwnFeed() {
        if(handle != null) {
            handle.createOffer(new IPluginHandleWebRTCCallbacks() {
                @Override
                public void onSuccess(JSONObject obj) {
                    try
                    {
                        JSONObject msg = new JSONObject();
                        JSONObject body = new JSONObject();
                        body.put("request", "configure");
                        body.put("audio", true);
                        body.put("video", true);
                        msg.put("message", body);
                        msg.put("jsep", obj);
                        handle.sendMessage(new PluginHandleSendMessageCallbacks(msg));
                    }catch (Exception ex) {

                    }
                }

                @Override
                public JSONObject getJsep() {
                    return null;
                }

                @Override
                public JanusMediaConstraints getMedia() {
                    JanusMediaConstraints cons = new JanusMediaConstraints();
                    cons.setRecvAudio(false);
                    cons.setRecvVideo(false);
                    cons.setSendAudio(true);
                    return cons;
                }

                @Override
                public Boolean getTrickle() {
                    return true;
                }

                @Override
                public void onCallbackError(String error) {

                }
            });
        }
    }

    private void registerUsername() {
        if(handle != null) {
            JSONObject obj = new JSONObject();
            JSONObject msg = new JSONObject();
            try
            {
                obj.put("request", "join");
                obj.put("room", roomid);
                obj.put("ptype", "publisher");
                obj.put("display", user_name);
                msg.put("message", obj);
            }
            catch(Exception ex)
            {

            }
            handle.sendMessage(new PluginHandleSendMessageCallbacks(msg));
        }
    }

    private void newRemoteFeed(BigInteger id) { //todo attach the plugin as a listener
        VideoRenderer.Callbacks myrenderer;
        if(!remoteRenderers.containsKey(id))
        {
            if(availableRemoteRenderers.empty())
            {
                //TODO no more space
                return;
            }
            remoteRenderers.put(id, availableRemoteRenderers.pop());
        }
        myrenderer = remoteRenderers.get(id);
        janusServer.Attach(new ListenerAttachCallbacks(id, myrenderer));
    }

    public class JanusPublisherPluginCallbacks implements IJanusPluginCallbacks {
        @Override
        public void success(JanusPluginHandle pluginHandle) {
            handle = pluginHandle;
            registerUsername();
        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsepLocal) {
            try
            {
                String event = msg.getString("videoroom");
                if(event.equals("joined")) {
                    myid = new BigInteger(msg.getString("id"));
                    publishOwnFeed();
                    if(msg.has("publishers")){
                        JSONArray pubs = msg.getJSONArray("publishers");
                        for(int i = 0; i < pubs.length(); i++) {
                            JSONObject pub = pubs.getJSONObject(i);
                            BigInteger tehId = new BigInteger(pub.getString("id"));
                            newRemoteFeed(tehId);
                        }
                    }
                } else if(event.equals("destroyed")) {

                } else if(event.equals("event")) {
                    if(msg.has("publishers")){
                        JSONArray pubs = msg.getJSONArray("publishers");
                        for(int i = 0; i < pubs.length(); i++) {
                            JSONObject pub = pubs.getJSONObject(i);
                            newRemoteFeed(new BigInteger(pub.getString("id")));
                        }
                    } else if(msg.has("leaving")) {

                    } else if(msg.has("unpublished")) {

                    } else {
                        //todo error
                    }
                }
                if(jsepLocal != null) {
                    handle.handleRemoteJsep(new PluginHandleWebRTCCallbacks(null, jsepLocal, false));
                }
            }
            catch (Exception ex)
            {

            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        }

        @Override
        public void onRemoteStream(MediaStream stream) {

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
            return JanusSupportedPluginPackages.JANUS_VIDEO_ROOM;
        }

        @Override
        public void onCallbackError(String error) {

        }

        @Override
        public void onDetached() {

        }
    }

    public class JanusGlobalCallbacks implements IJanusGatewayCallbacks {
        public void onSuccess() {
            janusServer.Attach(new JanusPublisherPluginCallbacks());
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
            //ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
            //iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
            return new ArrayList<PeerConnection.IceServer>();
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

    public VideoRoomTest(VideoRenderer.Callbacks localRender, VideoRenderer.Callbacks[] remoteRenders) {
        this.localRender = localRender;
        for(int i = 0; i < remoteRenders.length; i++)
        {
            this.availableRemoteRenderers.push(remoteRenders[i]);
        }
        janusServer = new JanusServer(new JanusGlobalCallbacks());
    }

    public boolean initializeMediaContext(Context context, boolean audio, boolean video, boolean videoHwAcceleration, EGLContext eglContext){
        return janusServer.initializeMediaContext(context, audio, video, videoHwAcceleration, eglContext);
    }

    public void Start() {
        janusServer.Connect();
    }

}
