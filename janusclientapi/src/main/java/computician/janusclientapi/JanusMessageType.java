package computician.janusclientapi;

/**
 * Created by ben.trent on 5/8/2015.
 */
public enum JanusMessageType {
    message,
    trickle,
    detach,
    destroy,
    keepalive,
    create,
    attach,
    event,
    error,
    ack,
    success,
    webrtcup,
    hangup,
    detached,
    media;

    @Override
    public String toString() {
        return name();
    }

    public boolean EqualsString(String type) {
        return this.toString() == type;
    }

    public static JanusMessageType fromString(String string) {
        return (JanusMessageType) valueOf(JanusMessageType.class, string.toLowerCase());
    }
}
