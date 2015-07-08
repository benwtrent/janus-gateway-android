package computician.janusclientapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusCreateSessionTransaction implements ITransactionCallbacks {

    private final IJanusSessionCreationCallbacks callbacks;
    public JanusCreateSessionTransaction(IJanusSessionCreationCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    public TransactionType getTransactionType()
    {
        return TransactionType.create;
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
                callbacks.onSessionCreationSuccess((obj));
            }
        }
        catch(JSONException ex)
        {
            callbacks.onCallbackError(ex.getMessage());
        }
    }
}
