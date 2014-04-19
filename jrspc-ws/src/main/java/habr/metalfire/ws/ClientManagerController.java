package habr.metalfire.ws;

import habr.metalfire.jrspc.ThrowableWriter;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("session")
public class ClientManagerController {

    final static Log log = LogFactory.getLog(ClientManagerController.class);

     
    @Autowired   
    private ClientManager clientManager;                   
         
           
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    private String initializeClientManager(HttpSession session) {
        JSONObject result = new JSONObject();   
        try{
            boolean loged = ClientManagersStorage.checkClientManager(clientManager, session) ;
            result.put("loged", loged);        
            result.put("clientManagerId", clientManager.getId());      
        }catch(Throwable th){
            result.put("error", th.toString()); 
            log.error("error="+new ThrowableWriter(th));
        }                
        log.debug("result="+result.toString());
        return result.toString();            
    }       


}