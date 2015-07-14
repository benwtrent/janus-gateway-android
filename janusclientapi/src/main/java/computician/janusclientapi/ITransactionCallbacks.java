package computician.janusclientapi;

import org.json.JSONObject;

/**
 * Created by ben.trent on 6/25/2015.
 */
public interface ITransactionCallbacks {
    void reportSuccess(JSONObject obj);

    TransactionType getTransactionType();
}
