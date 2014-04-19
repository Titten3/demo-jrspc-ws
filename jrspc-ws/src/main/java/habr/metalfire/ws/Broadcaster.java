package habr.metalfire.ws;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




public class Broadcaster{	    
    
    protected static Log log = LogFactory.getLog(Broadcaster.class);
    
	public static void broadcastCommand(String method) {	    
	    broadcastCommand(method, null);	    	    
	}

	
    public static void broadcastCommand(String method, Object params) {
        for (ClientManager clientManager : ClientManagersStorage.getClientManagers().values()) {
            clientManager.sendCommandToClient(method, params);
        }
    }    

    public static void sendCommandToUser(Long userId, String method, Object params) {     
        List<ClientManager> userClientManagers = ClientManagersStorage.findUserClientManagers(userId);
log.debug("sendCommandToUser: userClientManagers.size()="+userClientManagers.size());
        for(ClientManager clientManager: userClientManagers){
            clientManager.sendCommandToClient(method, params);
        }        
    }
	
}
