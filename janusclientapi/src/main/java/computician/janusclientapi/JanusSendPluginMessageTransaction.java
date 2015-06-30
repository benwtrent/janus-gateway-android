package computician.janusclientapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusSendPluginMessageTransaction implements ITransactionCallbacks {
    private final IJanusSendPluginMessageCallbacks callbacks;

    public JanusSendPluginMessageTransaction(IJanusSendPluginMessageCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public void reportSuccess(JSONObject obj)
    {
        try
        {
            JanusMessageType type = JanusMessageType.fromString(obj.getString("janus"));
            switch(type)
            {
                case success:
                {
                    JSONObject plugindata = obj.getJSONObject("plugindata");
                    JanusSupportedPluginPackages plugin = JanusSupportedPluginPackages.fromString(plugindata.getString("plugin"));
                    JSONObject data = plugindata.getJSONObject("data");
                    if(plugin == JanusSupportedPluginPackages.JANUS_NONE)
                    {
                        callbacks.onCallbackError("unexpected message: \n\t" + obj.toString());
                    }
                    else
                    {
                        callbacks.onJanusPluginMessageSynchronousSuccess(plugin, data);
                    }
                    break;
                }
                case ack:
                {
                    callbacks.onJanusPluginMessageAsynchronousAck();
                    break;
                }
                default:
                {
                    callbacks.onCallbackError(obj.getJSONObject("error").getString("reason"));
                    break;
                }
            }

        }
        catch (JSONException ex)
        {
            callbacks.onCallbackError(ex.getMessage());
        }
    }
}
