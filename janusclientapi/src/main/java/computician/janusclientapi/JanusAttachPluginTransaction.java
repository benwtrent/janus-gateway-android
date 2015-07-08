package computician.janusclientapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusAttachPluginTransaction implements ITransactionCallbacks {

    private final IJanusAttachPluginCallbacks callbacks;
    private final JanusSupportedPluginPackages plugin;
    private final IJanusPluginCallbacks pluginCallbacks;
    public JanusAttachPluginTransaction(IJanusAttachPluginCallbacks callbacks, JanusSupportedPluginPackages plugin, IJanusPluginCallbacks pluginCallbacks)
    {
        this.callbacks = callbacks;
        this.plugin = plugin;
        this.pluginCallbacks = pluginCallbacks;
    }

    public TransactionType getTransactionType()
    {
        return TransactionType.attach;
    }

    @Override
    public void reportSuccess(JSONObject obj)
    {
        try
        {
            JanusMessageType type = JanusMessageType.fromString(obj.getString("janus"));
            if(type != JanusMessageType.success)
            {
                callbacks.onCallbackError(obj.getJSONObject("error").getString("reason"));
            }
            else
            {
                callbacks.attachPluginSuccess(obj, plugin, pluginCallbacks);
            }
        }
        catch(JSONException ex)
        {
            callbacks.onCallbackError(ex.getMessage());
        }
    }

}
