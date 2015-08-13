package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
interface IJanusAttachPluginCallbacks extends IJanusCallbacks {
    void attachPluginSuccess(JSONObject obj, JanusSupportedPluginPackages plugin, IJanusPluginCallbacks callbacks);
}
