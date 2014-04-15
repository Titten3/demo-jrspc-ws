package habr.metalfire.ws;

import habr.metalfire.jrspc.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ClientManagersStorage {

    final static protected Log log = LogFactory.getLog(ClientManagersStorage.class);

    final static Map<String, ClientManager> clientManagers = new ConcurrentHashMap <String, ClientManager>();

    public static boolean checkClientManager(ClientManager clientManager, HttpSession session) {
        ClientManager registeredClientManager = findClientManager(clientManager.getId());
        boolean loged = true;
        if (registeredClientManager == null) {
            clientManager.setSession(session);
            addClientManager(clientManager);
            loged = false;
        }
        return loged;        
    }

    public static ClientManager findClientManager(String clientManagerId) {
        log.debug("findClientManager: " + clientManagerId + ", clientManagers.size()=" + clientManagers.size());
        return clientManagers.get(clientManagerId);
    }

    public static void removeClientManager(ClientManager clientManager) {        
        ClientManager removed  = clientManagers.remove(clientManager.getId());    
        if(removed != null){
            Broadcaster.broadcastCommand("userPanel.setOnlineCount", ClientManagersStorage.getClientManagersCount());
        }        
    }

    private static void addClientManager(ClientManager clientManager) {
        log.debug("addClientManager: " + clientManager.getId() + ", clientManagers.size()=" + clientManagers.size());
        clientManagers.put(clientManager.getId(), clientManager);
        Broadcaster.broadcastCommand("userPanel.setOnlineCount", ClientManagersStorage.getClientManagersCount());
    }


    public static ClientManager findUserClientManager(Long userId) {
        for (ClientManager clientManager : clientManagers.values()) {
            User user = clientManager.getUser();
            if (user != null && user.getId().equals(userId)) {
                return clientManager;
            }
        }
        return null;
    }

    public static int getClientManagersCount() {
        return clientManagers.size();
    }

}
