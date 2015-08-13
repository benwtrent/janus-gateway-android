package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
interface IPluginHandleSendMessageCallbacks extends IJanusCallbacks {
    void onSuccessSynchronous(JSONObject obj);

    void onSuccesAsynchronous();

    JSONObject getMessage();
}
