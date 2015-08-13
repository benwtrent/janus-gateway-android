package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
interface IJanusSessionCreationCallbacks extends IJanusCallbacks {
    void onSessionCreationSuccess(JSONObject obj);
}
