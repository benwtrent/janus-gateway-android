package computician.janusclientapi;

/**
 * Created by ben.trent on 6/25/2015.
 */
class JanusTransactionCallbackFactory {

    public static ITransactionCallbacks createNewTransactionCallback(JanusServer server, TransactionType type) {
        switch (type) {
            case create:
                return new JanusCreateSessionTransaction(server);
            default:
                return null;
        }
    }

    public static ITransactionCallbacks createNewTransactionCallback(JanusServer server, TransactionType type, JanusSupportedPluginPackages plugin, IPluginHandleWebRTCCallbacks callbacks) {
        switch (type) {
            case plugin_handle_webrtc_message:
                return new JanusWebRtcTransaction(plugin, callbacks);
            default:
                return null;
        }
    }

    public static ITransactionCallbacks createNewTransactionCallback(JanusServer server, TransactionType type, JanusSupportedPluginPackages plugin, IPluginHandleSendMessageCallbacks callbacks) {
        switch (type) {
            case plugin_handle_message:
                return new JanusSendPluginMessageTransaction(plugin, callbacks);
            default:
                return null;
        }
    }

    public static ITransactionCallbacks createNewTransactionCallback(JanusServer server, TransactionType type, JanusSupportedPluginPackages plugin, IJanusPluginCallbacks callbacks) {
        switch (type) {
            case create:
                return new JanusCreateSessionTransaction(server);
            case attach:
                return new JanusAttachPluginTransaction(server, plugin, callbacks);
            default:
                return null;
        }
    }
}
