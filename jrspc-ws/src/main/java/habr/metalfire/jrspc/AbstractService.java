package habr.metalfire.jrspc;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** parent class for all services */

public abstract 
class AbstractService implements UserAware, Serializable{

    private static final long serialVersionUID = 1L;

    protected  Log log = LogFactory.getLog(this.getClass());
    
    private Object user;

    @Override              
    public void setUser(Object user) {          
         this.user = user;
    }
            
    @SuppressWarnings("unchecked")
    @Override 
    public <T> T  getUser() {          
        return   (T) user;
    }  
    
}
