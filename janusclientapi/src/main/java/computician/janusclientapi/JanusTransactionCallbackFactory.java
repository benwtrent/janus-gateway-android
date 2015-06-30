package computician.janusclientapi;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusTransactionCallbackFactory {
    public static ITransactionCallbacks createNewTransactionCallback(JanusServer server, JanusMessageType type)
    {
        switch(type)
        {
            case create:
                return new JanusCreateSessionTransaction(server);
            case attach:
                return new JanusAttachPluginTransaction(server);
            case message:
                return new JanusSendPluginMessageTransaction(server);
            default:
                return null;
        }
    }
}
