package computician.janusclientapi;

import org.json.JSONObject;

import java.math.BigInteger;

/**
 * Created by ben.trent on 6/30/2015.
 */
public interface IJanusServerPluginMessageInterface {
    void sendMessage(TransactionType type, BigInteger handle, IPluginHandleSendMessageCallbacks callbacks, JanusSupportedPluginPackages plugin);
    void sendMessage(TransactionType type, BigInteger handle, IPluginHandleWebRTCCallbacks callbacks, JanusSupportedPluginPackages plugin);
}
