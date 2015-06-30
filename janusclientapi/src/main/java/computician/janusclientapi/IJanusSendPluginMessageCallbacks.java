package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public interface IJanusSendPluginMessageCallbacks extends IJanusCallbacks {
    void onJanusPluginMessageSynchronousSuccess(JanusSupportedPluginPackages plugin, JSONObject data);
    void onJanusPluginMessageAsynchronousAck();
}
