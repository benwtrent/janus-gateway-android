package computician.janusclientapi;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRendererGui;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusServer implements Runnable, IJanusServerPluginMessageInterface, IJanusMessageObserver, IJanusSessionCreationCallbacks, IJanusAttachPluginCallbacks {

    private class RandomString{
        final String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final Random rnd = new Random();
        public String randomString(Integer length)
        {
            StringBuilder sb = new StringBuilder(length);
            for(int i = 0; i < length; i++)
            {
                sb.append(str.charAt(rnd.nextInt(str.length())));
            }
            return sb.toString();
        }
    }

    private final RandomString stringGenerator  = new RandomString();
    private Hashtable<BigInteger, JanusPluginHandle> attachedPlugins = new Hashtable<BigInteger, JanusPluginHandle>();
    private Object attachedPluginsLock = new Object();
    private Hashtable<String, ITransactionCallbacks> transactions = new Hashtable<String, ITransactionCallbacks>();
    private Object transactionsLock = new Object();
    public final String serverUri;
    public final IJanusGatewayCallbacks gatewayObserver;
    public final List<PeerConnection.IceServer> iceServers;
    public final Boolean ipv6Support;
    public final Integer maxPollEvents;
    private BigInteger sessionId;
    private Boolean connected;
    private final IJanusMessenger serverConnection;
    private volatile Thread keep_alive;
    public JanusServer(IJanusGatewayCallbacks gatewayCallbacks) throws URISyntaxException {
        gatewayObserver = gatewayCallbacks;
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        serverUri = gatewayObserver.getServerUri();
        iceServers = gatewayObserver.getIceServers();
        ipv6Support = gatewayObserver.getIpv6Support();
        maxPollEvents = gatewayObserver.getMaxPollEvents();
        connected = false;
        sessionId = new BigInteger("-1");
        serverConnection = JanusMessagerFactory.createMessager(serverUri, this);
    }

    private String putNewTransaction(ITransactionCallbacks transactionCallbacks)
    {
        String transaction = stringGenerator.randomString(12);
        synchronized (transactionsLock) {
            while(transactions.containsKey(transaction))
                transaction = stringGenerator.randomString(12);
            transactions.put(transaction, transactionCallbacks);
        }
        return transaction;
    }

    private void createSession()
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("janus", JanusMessageType.create);
            ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, TransactionType.create);
            String transaction = putNewTransaction(cb);
            obj.put("transaction", transaction);
            serverConnection.sendMessage(obj.toString());
        }
        catch(JSONException ex)
        {
            onCallbackError(ex.getMessage());
        }
    }

    public void run()
    {
        Thread thisThread = Thread.currentThread();
        while(keep_alive == thisThread) {
            try
            {
                thisThread.sleep(29000);
            }
            catch (InterruptedException ex)
            {}
            if (!connected || serverConnection.getMessengerType() != JanusMessengerType.websocket)
                return;
            JSONObject obj = new JSONObject();
            try
            {
                obj.put("janus", JanusMessageType.keepalive.toString());
                obj.put("session_id", sessionId);
                obj.put("transaction", stringGenerator.randomString(12));
                serverConnection.sendMessage(obj.toString());
            }
            catch (JSONException ex)
            {
                //todo
                return;
            }
        }
    }

    public Boolean isConnected()
    {
        return connected;
    }

    public BigInteger getSessionId()
    {
        return sessionId;
    }

    public void Attach(IJanusPluginCallbacks callbacks)
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("janus", JanusMessageType.attach);
            obj.put("plugin", callbacks.getPlugin());
            if (serverConnection.getMessengerType() == JanusMessengerType.websocket)
                obj.put("session_id", sessionId);
            ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, TransactionType.attach, callbacks.getPlugin(), callbacks);
            String transaction = putNewTransaction(cb);
            obj.put("transaction", transaction);
            serverConnection.sendMessage(obj.toString());
        }
        catch(JSONException ex)
        {
            onCallbackError(ex.getMessage());
        }
    }

    public void Destroy()
    {
        serverConnection.disconnect();
        gatewayObserver.onDestroy();
        //TODO
    }

    public void newMessageForPlugin(String message, BigInteger plugin_id)
    {
        JanusPluginHandle handle = null;
        synchronized (attachedPluginsLock){
            handle = attachedPlugins.get(plugin_id);
        }
        if(handle != null)
        {
            handle.onMessage(message);
        }
    }

    @Override
    public void onCallbackError(String msg)
    {
        gatewayObserver.onCallbackError(msg);
        //TODO
    }

    //region ServerPluginMessage

    @Override
    public void sendMessage(TransactionType type, BigInteger handle, IPluginHandleSendMessageCallbacks callbacks, JanusSupportedPluginPackages plugin)
    {
        JSONObject msg = callbacks.getMessage();
        if(msg != null)
        {
            try
            {
                JSONObject newMessage = new JSONObject();
                newMessage.put("janus", JanusMessageType.message.toString());

                if(serverConnection.getMessengerType() == JanusMessengerType.websocket)
                {
                    newMessage.put("session_id", sessionId);
                    newMessage.put("handle_id", handle);
                }
                ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, TransactionType.plugin_handle_message, plugin, callbacks);
                String transaction = putNewTransaction(cb);
                newMessage.put("transaction", transaction);
                if(msg.has("message"))
                    newMessage.put("body", msg.getJSONObject("message"));
                if(msg.has("jsep"))
                    newMessage.put("jsep", msg.getJSONObject("jsep"));
                serverConnection.sendMessage(newMessage.toString());
            }
            catch(JSONException ex)
            {
                callbacks.onCallbackError(ex.getMessage());
            }
        }
    }

    @Override
    public void sendMessage(TransactionType type, BigInteger handle, IPluginHandleWebRTCCallbacks callbacks, JanusSupportedPluginPackages plugin)
    {
        try
        {
            JSONObject msg = new JSONObject();
            if(serverConnection.getMessengerType() == JanusMessengerType.websocket)
            {
                msg.put("session_id", sessionId);
                msg.put("handle_id", handle);
            }
            ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, TransactionType.plugin_handle_webrtc_message, plugin, callbacks);
            String transaction = putNewTransaction(cb);
            msg.put("transaction", transaction);
            if(callbacks.getJsep() != null)
            {
                msg.put("jsep", callbacks.getJsep());
            }

        }
        catch(JSONException ex)
        {

        }
    }

    //endregion

    //region MessageObserver
    @Override
    public void receivedNewMessage(JSONObject obj)
    {
        try
        {
            JanusMessageType type = JanusMessageType.fromString(obj.getString("janus"));
            String transaction = null;
            BigInteger sender = null;
            if(obj.has("transaction"))
            {
                transaction = obj.getString("transaction");
            }
            if(obj.has("sender"))
                sender = (BigInteger) obj.get("sender");
            JanusPluginHandle handle = null;
            if(sender != null)
            {
                synchronized (attachedPluginsLock) {
                    handle = attachedPlugins.get(sender);
                }
            }
            switch(type)
            {
                case keepalive:
                    break;
                case ack:
                case success:
                case error:
                {
                    if(transaction != null)
                    {
                        ITransactionCallbacks cb = null;
                        synchronized (transactionsLock) {
                            cb = transactions.get(transaction);
                            if(cb != null)
                                transactions.remove(transaction);
                        }
                        if(cb != null)
                        {
                            cb.reportSuccess(obj);
                            transactions.remove(transaction);
                        }
                    }
                    break;
                }
                case detached:
                {
                    if(handle != null)
                    {
                        handle.onDetached();
                        handle.detach();
                    }
                    break;
                }
                case event:
                {
                    if(handle != null) {
                        JSONObject plugin_data = null;
                        if (obj.has("plugindata"))
                            plugin_data = obj.getJSONObject("plugindata");
                        if (plugin_data != null) {
                            JSONObject data = null;
                            JSONObject jsep = null;
                            if (plugin_data.has("data"))
                                data = plugin_data.getJSONObject("data");
                            if (obj.has("jsep"))
                                jsep = obj.getJSONObject("jsep");
                            handle.onMessage(data, jsep);
                        }
                    }
                }
            }
        }
        catch (JSONException ex)
        {
            //TODO do we want to alert the server controller?
        }
    }

    @Override
    public void onOpen()
    {
        createSession();
        //TODO
    }

    @Override
    public void onClose()
    {
        connected = false;
        gatewayObserver.onCallbackError("Connection to janus server is closed");
        //TODO
    }

    @Override
    public void onError(Exception ex)
    {
        gatewayObserver.onCallbackError("Error connected to Janus gateway. Exception: " + ex.getMessage());
        //TODO
    }
    //endregion

    //region SessionCreationCallbacks
    @Override
    public void onSessionCreationSuccess(JSONObject obj)
    {
        try
        {
            sessionId = (BigInteger)obj.getJSONObject("data").get("id");
            keep_alive = new Thread(this, "KeepAlive");
            keep_alive.start();
            connected = true;
            //TODO do we want to keep track of multiple sessions and servers?
            gatewayObserver.onSuccess();
        }
        catch(JSONException ex)
        {
            gatewayObserver.onCallbackError(ex.getMessage());
        }
        //TODO
    }

    //endregion

    //region AttachPluginCallbacks

    @Override
    public void attachPluginSuccess(JSONObject obj, JanusSupportedPluginPackages plugin, IJanusPluginCallbacks pluginCallbacks)
    {
        try
        {
            BigInteger handle = (BigInteger)obj.getJSONObject("data").get("id");
            JanusPluginHandle pluginHandle = new JanusPluginHandle(this, plugin, handle, pluginCallbacks);
            synchronized (attachedPluginsLock) {
                attachedPlugins.put(handle, pluginHandle);
            }
            pluginCallbacks.success(pluginHandle);
        }
        catch(JSONException ex)
        {

        }
        //TODO
    }

    //endregion

}
