package computician.janusclientapi;

import org.webrtc.SessionDescription;

/**
 * Created by ben.trent on 7/8/2015.
 */
public interface IJanusWebRtcObserver {
    void onLocalSdp(SessionDescription sdp, IPluginHandleWebRTCCallbacks callbacks);
}
