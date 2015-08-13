package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 8/12/2015.
 */
public class PluginHandleSendMessageCallbacks implements IPluginHandleSendMessageCallbacks {

    private final JSONObject message;

    public PluginHandleSendMessageCallbacks(JSONObject message) {
        this.message = message;
    }

    @Override
    public void onSuccessSynchronous(JSONObject obj) {
    }

    @Override
    public void onSuccesAsynchronous() {
    }

    @Override
    public JSONObject getMessage() {
        return message;
    }

    @Override
    public void onCallbackError(String error) {
    }
}
