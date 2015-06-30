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
    public boolean EqualsString(String type)
    {
        return this.toString() == type;
    }
    public static JanusMessageType fromString(String string){
        if(JanusMessageType.ack.EqualsString(string))
            return JanusMessageType.ack;
        else if(JanusMessageType.message.EqualsString(string))
            return JanusMessageType.message;
        else if(JanusMessageType.trickle.EqualsString(string))
            return JanusMessageType.trickle;
        else if(JanusMessageType.detach.EqualsString(string))
            return JanusMessageType.detach;
        else if(JanusMessageType.destroy.EqualsString(string))
            return JanusMessageType.destroy;
        else if(JanusMessageType.keepalive.EqualsString(string))
            return JanusMessageType.keepalive;
        else if(JanusMessageType.create.EqualsString(string))
            return JanusMessageType.create;
        else if(JanusMessageType.attach.EqualsString(string))
            return JanusMessageType.attach;
        else if(JanusMessageType.event.EqualsString(string))
            return JanusMessageType.event;
        else if(JanusMessageType.error.EqualsString(string))
            return JanusMessageType.error;
        else if(JanusMessageType.success.EqualsString(string))
            return JanusMessageType.success;
        else if(JanusMessageType.webrtcup.EqualsString(string))
            return JanusMessageType.webrtcup;
        else if(JanusMessageType.hangup.EqualsString(string))
            return JanusMessageType.hangup;
        else if(JanusMessageType.detached.EqualsString(string))
            return JanusMessageType.detached;
        else
            return JanusMessageType.media;
    }
}
