package habr.metalfire.jrspc;

import habr.metalfire.ws.ClientManager;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** parent class for all services */

public abstract class AbstractService implements Serializable{

    private static final long serialVersionUID = 1L;

    protected  Log log = LogFactory.getLog(this.getClass());


    private ClientManager clientManager;    
            
  
    public <T> T  getUser() {          
        return clientManager.getUser();
    }
        
    public  ClientManager getClientManager() {       
        return   clientManager;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public void putSessionVariable(String key, Object value) {
        getClientManager().putSessionVariable(key, value);        
    }
    
  
    public <T> T getSessionVariable(String key) {
         return getClientManager().getSessionVariable(key);        
    }
    
}
