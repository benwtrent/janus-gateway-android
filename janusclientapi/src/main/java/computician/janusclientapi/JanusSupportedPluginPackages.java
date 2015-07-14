package computician.janusclientapi;

/**
 * Created by ben.trent on 5/8/2015.
 */
public enum JanusSupportedPluginPackages {
    JANUS_AUDIO_BRIDGE("janus.plugin.audiobridge"),
    JANUS_ECHO_TEST("janus.plugin.echotest"),
    JANUS_RECORD_PLAY("janus.plugin.recordplay"),
    JANUS_STREAMING("janus.plugin.streaming"),
    JANUS_SIP("janus.plugin.sip"),
    JANUS_VIDEO_CALL("janus.plugin.videocall"),
    JANUS_VIDEO_ROOM("janus.plugin.videoroom"),
    JANUS_VOICE_MAIL("janus.plugin.voicemail"),
    JANUS_NONE("none");

    @Override
    public String toString() {
        return plugin_name;
    }

    public boolean EqualsString(String str) {
        return plugin_name == str;
    }

    private JanusSupportedPluginPackages(String plugin_name) {
        this.plugin_name = plugin_name;
    }

    public static JanusSupportedPluginPackages fromString(String string) {
        if (JANUS_AUDIO_BRIDGE.EqualsString(string))
            return JANUS_AUDIO_BRIDGE;
        else if (JANUS_ECHO_TEST.EqualsString(string))
            return JANUS_ECHO_TEST;
        else if (JANUS_RECORD_PLAY.EqualsString(string))
            return JANUS_RECORD_PLAY;
        else if (JANUS_STREAMING.EqualsString(string))
            return JANUS_STREAMING;
        else if (JANUS_SIP.EqualsString(string))
            return JANUS_SIP;
        else if (JANUS_VIDEO_CALL.EqualsString(string))
            return JANUS_VIDEO_CALL;
        else if (JANUS_VIDEO_ROOM.EqualsString(string))
            return JANUS_VIDEO_ROOM;
        else if (JANUS_VOICE_MAIL.EqualsString(string))
            return JANUS_VOICE_MAIL;
        else
            return JANUS_NONE;
    }

    private final String plugin_name;
}
