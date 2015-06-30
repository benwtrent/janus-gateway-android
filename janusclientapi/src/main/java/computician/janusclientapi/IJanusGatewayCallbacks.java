package computician.janusclientapi;

import java.util.List;

/**
 * Created by ben.trent on 5/7/2015.
 */
public interface IJanusGatewayCallbacks extends IJanusCallbacks {
    public void onSuccess();
    public void onDestroy();
    public String getServerUri();
    public List<JanusIceServer> getIceServers();
    public Boolean getIpv6Support();
    public Integer getMaxPollEvents();
}
