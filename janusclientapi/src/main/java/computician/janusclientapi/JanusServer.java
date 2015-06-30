package computician.janusclientapi;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SessionDescription;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Created by ben.trent on 5/7/2015.
 */
public class JanusServer implements IJanusMessageObserver, IJanusSessionCreationCallbacks, IJanusAttachPluginCallbacks, IJanusSendPluginMessageCallbacks {

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
    public final List<JanusIceServer> iceServers;
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

    private void createSession()
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("janus", JanusMessageType.create);
            String transaction = stringGenerator.randomString(12);
            obj.put("transaction", transaction);
            ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, JanusMessageType.create);
            transactions.put(transaction, cb);
            serverConnection.sendMessage(obj.toString());
        }
        catch(JSONException ex)
        {
            onError(ex);
        }
    }

    private void keepAlive()
    {
        Thread thisThread = Thread.currentThread();
        while(keep_alive == thisThread) {
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
            try
            {
                thisThread.sleep(29000);
            }
            catch (InterruptedException ex)
            {}
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

    public void Attach(IJanusPluginCallbacks callbacks) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("janus", JanusMessageType.attach);
        obj.put("plugin", callbacks.getPlugin());
        if(serverConnection.getMessengerType() == JanusMessengerType.websocket)
            obj.put("session_id", sessionId);
        String transaction = stringGenerator.randomString(12);
        obj.put("transaction", transaction);
        //todo

    }

    public void Destroy()
    {
        //TODO
    }


    public void newMessageForPlugin(String message, Long plugin_id)
    {
        if(attachedPlugins.containsKey(plugin_id))
        {
            //todo determine message type and how we want to handle it
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

        }
    }

    @Override
    public void onOpen()
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("janus", JanusMessageType.create.toString());
            String transaction = stringGenerator.randomString(12);
            ITransactionCallbacks cb = JanusTransactionCallbackFactory.createNewTransactionCallback(this, JanusMessageType.create);
            synchronized (transactionsLock) {
                while (transactions.containsKey(transaction))
                    transaction = stringGenerator.randomString(12);
                transactions.put(transaction, cb);
            }
            obj.put("transaction", transaction);
            serverConnection.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //TODO
    }

    @Override
    public void onClose()
    {
        connected = false;
        //TODO
    }

    @Override
    public void onError(Exception ex)
    {
        //TODO
    }
    //endregion

    @Override
    public void onCallbackError(String msg)
    {
        //TODO
    }


    //region SessionCreationCallbacks
    @Override
    public void onSessionCreationSuccess(JSONObject obj)
    {
        //TODO
    }
    //endregion

    @Override
    public void attachPluginSuccess(JSONObject obj)
    {
        //TODO
    }
    //endregion

    //region SendPluginMessageCallbacks

    @Override
    public void onJanusPluginMessageSynchronousSuccess(JanusSupportedPluginPackages plugin, JSONObject data)
    {
        //TODO
    }

    @Override
    public void onJanusPluginMessageAsynchronousAck()
    {
        //TODO
    }

    //endregion

}
