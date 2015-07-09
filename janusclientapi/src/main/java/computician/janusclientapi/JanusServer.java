package computician.janusclientapi;

import android.content.Context;

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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusServer implements Runnable, IJanusMessageObserver, IJanusSessionCreationCallbacks, IJanusAttachPluginCallbacks {

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
    private ConcurrentHashMap<BigInteger, JanusPluginHandle> attachedPlugins = new ConcurrentHashMap<BigInteger, JanusPluginHandle>();
    private Object attachedPluginsLock = new Object();
    private ConcurrentHashMap<String, ITransactionCallbacks> transactions = new ConcurrentHashMap<String, ITransactionCallbacks>();
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
    private Boolean peerConnectionFactoryInitialized = false;
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

    public boolean initializeMediaContext(Context context, boolean audio, boolean video, boolean videoHwAcceleration, EGLContext eglContext)
    {
        if(!PeerConnectionFactory.initializeAndroidGlobals(context, audio, video, videoHwAcceleration, eglContext))
            return false;
        peerConnectionFactoryInitialized = true;
        return true;
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
                if(serverConnection.getMessengerType() == JanusMessengerType.websocket)
                    obj.put("session_id", sessionId);
                obj.put("transaction", stringGenerator.randomString(12));
                serverConnection.sendMessage(obj.toString(), sessionId);
            }
            catch (JSONException ex)
            {
                gatewayObserver.onCallbackError("Keep alive failed is Janus online?" + ex.getMessage());
                connected = false;
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
        if(!peerConnectionFactoryInitialized)
        {
            callbacks.onCallbackError("Peerconnection factory is not initialized, please initialize via initializeMediaContext so that peerconnections can be made by the plugins");
            return;
        }
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
            serverConnection.sendMessage(obj.toString(), sessionId);
        }
        catch(JSONException ex)
        {
            onCallbackError(ex.getMessage());
        }
    }

    public void Destroy()
    {
        serverConnection.disconnect();
        keep_alive = null;
        connected = false;
        gatewayObserver.onDestroy();
        for(ConcurrentHashMap.Entry<BigInteger, JanusPluginHandle> handle : attachedPlugins.entrySet())
        {
            handle.getValue().detach();
        }
        synchronized (transactionsLock) {
            for(Object trans : transactions.entrySet())
                transactions.remove(trans);
        }
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
    }


    public void sendMessage(JSONObject msg, JanusMessageType type, BigInteger handle)
    {
        try
        {
            msg.put("janus", type.toString());
            if(serverConnection.getMessengerType() == JanusMessengerType.websocket) {
                msg.put("session_id", sessionId);
                if(BigInteger.valueOf(0).compareTo(handle) > 0)
                    msg.put("handle_id", handle);
            }
            msg.put("transaction", stringGenerator.randomString(12));
            if(connected)
                serverConnection.sendMessage(msg.toString(), sessionId, handle);
            if(type == JanusMessageType.detach)
            {
                synchronized (attachedPluginsLock){
                    if(attachedPlugins.containsKey(handle))
                        attachedPlugins.remove(handle);
                }
            }
        }
        catch (JSONException ex)
        {
            gatewayObserver.onCallbackError(ex.getMessage());
        }
    }

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
                serverConnection.sendMessage(newMessage.toString(), sessionId, handle);
            }
            catch(JSONException ex)
            {
                callbacks.onCallbackError(ex.getMessage());
            }
        }
    }

    public void sendMessage(TransactionType type, BigInteger handle, IPluginHandleWebRTCCallbacks callbacks, JanusSupportedPluginPackages plugin)
    {
        try
        {
            JSONObject msg = new JSONObject();
            msg.put("janus", JanusMessageType.message.toString());
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
            serverConnection.sendMessage(msg.toString(), sessionId, handle);
        }
        catch(JSONException ex)
        {
            callbacks.onCallbackError(ex.getMessage());
        }
    }

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
            gatewayObserver.onCallbackError(ex.getMessage());
        }
    }

    @Override
    public void onOpen()
    {
        createSession();
    }

    @Override
    public void onClose()
    {
        connected = false;
        gatewayObserver.onCallbackError("Connection to janus server is closed");
    }

    @Override
    public void onError(Exception ex)
    {
        gatewayObserver.onCallbackError("Error connected to Janus gateway. Exception: " + ex.getMessage());
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
            //or do we want to use the pluginCallbacks.error(ex.getMessage());
            gatewayObserver.onCallbackError(ex.getMessage());
        }
    }

    //endregion

}
