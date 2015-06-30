package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public interface IPluginHandleSendMessageCallbacks extends IJanusCallbacks {
    void onSuccessSynchronous(JSONObject obj);
    void onSuccesAsynchronous();
    JSONObject getJsep();
    JSONObject getMessage();
}
