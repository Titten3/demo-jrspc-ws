package habr.metalfire.ws;

import habr.metalfire.jrspc.User;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ClientManagersStorage {

    final static Log log = LogFactory.getLog(ClientManagersStorage.class);

    final static Map<String, ClientManager> clientManagers = new HashMap<String, ClientManager>();

    public static boolean checkClientManager(ClientManager clientManager, HttpSession session) {
        String sessionManagerId = clientManager.getId();
        ClientManager registeredClientManager = findClientManager(sessionManagerId);
        boolean loged = true;
        if (registeredClientManager == null) {
            clientManager.setSession(session);
            addClientManager(sessionManagerId, clientManager);
            loged = false;
        }
        return loged;        
    }

    public static ClientManager findClientManager(String clientManagerId) {
        log.debug("findClientManager: " + clientManagerId + ", clientManagers.size()=" + clientManagers.size());
        return clientManagers.get(clientManagerId);
    }

    public static void removeClientManager(ClientManager clientManager) {
        String clientManagerId = String.valueOf(clientManager.hashCode());
        clientManagers.remove(clientManagerId);     
    }

    private static void addClientManager(String clientManagerId, ClientManager clientManager) {
        log.debug("addClientManager: " + clientManagerId + ", clientManagers.size()=" + clientManagers.size());
        clientManagers.put(clientManagerId, clientManager);
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

}
