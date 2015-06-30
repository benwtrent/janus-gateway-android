package computician.janusclientapi;

import org.webrtc.DataChannel;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusWebRtcStuff {

    private boolean started = false;
    private MediaStream myStream = null;
    private SessionDescription mySdp = null;
    private PeerConnection pc = null;
    private DataChannel dataChannel = null;
    private boolean trickle = true;
    private boolean iceDone = false;
    private boolean sdpSend = false;

    public JanusWebRtcStuff() {}

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public MediaStream getMyStream() {
        return myStream;
    }

    public void setMyStream(MediaStream myStream) {
        this.myStream = myStream;
    }

    public SessionDescription getMySdp() {
        return mySdp;
    }

    public void setMySdp(SessionDescription mySdp) {
        this.mySdp = mySdp;
    }

    public PeerConnection getPc() {
        return pc;
    }

    public void setPc(PeerConnection pc) {
        this.pc = pc;
    }

    public DataChannel getDataChannel() {
        return dataChannel;
    }

    public void setDataChannel(DataChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

    public boolean isTrickle() {
        return trickle;
    }

    public void setTrickle(boolean trickle) {
        this.trickle = trickle;
    }

    public boolean isIceDone() {
        return iceDone;
    }

    public void setIceDone(boolean iceDone) {
        this.iceDone = iceDone;
    }

    public boolean isSdpSend() {
        return sdpSend;
    }

    public void setSdpSend(boolean sdpSend) {
        this.sdpSend = sdpSend;
    }
}
