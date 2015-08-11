package computician.janusclient;

import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import computician.janusclientapi.IJanusCallbacks;
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
public class VideoRoomTest {
    private final String JANUS_URI = "ws://192.168.1.197:8188";
    private JanusPluginHandle handle = null;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks[] remoteRenders;
    private JanusServer janusServer;

    private void publishOwnFeed() {

    }

    private void newRemoteFeed(BigInteger id) { //todo attach the plugin as a listener

    }


    public class JanusPublisherPluginCallbacks implements IJanusPluginCallbacks {
        @Override
        public void success(JanusPluginHandle pluginHandle) {
            handle = pluginHandle;
        }

        @Override
        public void onMessage(JSONObject msg, final JSONObject jsepLocal) {
            try
            {
                String event = msg.getString("videoroom");
                if(event == "joined") {
                    publishOwnFeed();
                } else if(event == "destroyed") {

                } else if(event == "event") {

                }
                if(jsepLocal != null) {
                    handle.handleRemoteJsep(new IPluginHandleWebRTCCallbacks() {

                        @Override
                        public void onSuccess(JSONObject obj) {

                        }

                        @Override
                        public JSONObject getJsep() {
                            return jsepLocal;
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
            catch (Exception ex)
            {

            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
            VideoRendererGui.update(localRender, 0, 0, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
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

    public VideoRoomTest(VideoRenderer.Callbacks localRender, VideoRenderer.Callbacks[] remoteRenders) {
        this.localRender = localRender;
        this.remoteRenders = remoteRenders;
        janusServer = new JanusServer(new JanusGlobalCallbacks());
    }

}
