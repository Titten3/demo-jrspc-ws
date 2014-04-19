package habr.metalfire.ws;

import habr.metalfire.jrspc.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ClientManagersStorage {

    final static protected Log log = LogFactory.getLog(ClientManagersStorage.class);

    final static private  Map<String, ClientManager> clientManagers = new ConcurrentHashMap <String, ClientManager>();

    public static boolean checkClientManager(ClientManager clientManager, HttpSession session) {
        ClientManager registeredClientManager = findClientManagerById(clientManager.getId());  
        if (registeredClientManager == null) {
            clientManager.setSession(session);
            addClientManager(clientManager);          
            registeredClientManager = clientManager;
        }
        return registeredClientManager.getUser() != null;        
    }

    public static ClientManager findClientManagerById(String clientManagerId) {
        //log.debug("findClientManager: " + clientManagerId + ", clientManagers.size()=" + clientManagers.size());
        return clientManagers.get(clientManagerId);
    }

    
    /** called from timeout after ClientManager.setSession, if client not connected via websocket, 
     * or at websocket connection closing */
    
    public static void removeClientManager(ClientManager clientManager) {        
        ClientManager removed  = clientManagers.remove(clientManager.getId());    
        if(removed == null){return;}
        User user = removed.getUser();
        if(user != null){                
            Broadcaster.broadcastCommand("userPanel.refreshLogedUsers", ClientManagersStorage.findLogedUsersLogins());   
        }                
        Broadcaster.broadcastCommand("userPanel.setOnlineCount", ClientManagersStorage.getClientManagersCount());            
        try {
            clientManager.getSession().invalidate();
            clientManager.setSession(null);     
        } catch (Throwable th) {
            log.error("at removeClientManager: " + th);
        }        
    }

    private static void addClientManager(ClientManager clientManager) {
        //log.debug("addClientManager: " + clientManager.getId() + ", clientManagers.size()=" + clientManagers.size());
        getClientManagers().put(clientManager.getId(), clientManager);
        Broadcaster.broadcastCommand("userPanel.setOnlineCount", ClientManagersStorage.getClientManagersCount());
    }
    
    public static List<String> findLogedUsersLogins() {
        List <String> logedUsersLogins = new ArrayList<String>();
        for (ClientManager clientManager : clientManagers.values()) {
            User user = clientManager.getUser();
            if (user != null && !logedUsersLogins.contains(user.getLogin())) {
                logedUsersLogins.add(user.getLogin());
            }
        }               
        return logedUsersLogins;        
    }

    public static List<ClientManager> findUserClientManagers(Long userId) {
        List <ClientManager> userClientManagers = new ArrayList<ClientManager>();
        for (ClientManager clientManager : clientManagers.values()) {
            User user = clientManager.getUser();
            if (user != null && user.getId().equals(userId)) {
                userClientManagers.add(clientManager);
            }
        }
        return userClientManagers;
    }

    public static int getClientManagersCount() {
        return getClientManagers().size();
    }

    public static Map<String, ClientManager> getClientManagers() {
        return clientManagers;
    }

}
