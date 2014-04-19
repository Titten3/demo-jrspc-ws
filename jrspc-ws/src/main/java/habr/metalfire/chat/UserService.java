package habr.metalfire.chat;

import habr.metalfire.jrspc.AbstractService;
import habr.metalfire.jrspc.Remote;
import habr.metalfire.jrspc.Secured;
import habr.metalfire.jrspc.User;
import habr.metalfire.jrspc.UserManager;
import habr.metalfire.ws.Broadcaster;
import habr.metalfire.ws.ClientManagersStorage;

import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UserService extends AbstractService {

    private static final long serialVersionUID = 1L;

    //public static AtomicInteger logedCount = new AtomicInteger(0);
        
    @Autowired
    private UserManager userManager;    
    

               
    @Remote
    public Long registerUser(User user){               
        //log.debug("registerUser: user="+JSONObject.fromObject(user));
        if(userManager.findByLogin(user.getLogin()) != null){
          throw new RuntimeException("User with login "+user.getLogin()+" already registered!");
        }           
        if(userManager.getUsersCount() == 0){
          user.setRole(User.Role.Admin.name());
        }else{
          user.setRole(User.Role.User.name());
        } 
        userManager.saveUser(user); 
        return user.getId();
    }          

    
    @Remote
    public User logIn(String login, String password){     
        //
        log.debug("logIn: login="+login+", password="+password);
         String error = "Unknown combination of login and password!";
         User user = userManager.findByLogin(login);
         log.debug("logIn: user="+JSONObject.fromObject(user));
         if(user == null){ throw new RuntimeException(error);}
         if(!user.getPassword().equals(password)){ throw new RuntimeException(error);} 
         getClientManager().putSessionVariable("user", user);         
         Broadcaster.broadcastCommand("userPanel.refreshLogedUsers", ClientManagersStorage.findLogedUsersLogins());
         return user;
    }     
    

    @Secured("User") 
    @Remote
    public void logOut(){       
         getClientManager().removeSessionVariable("user");         
         Broadcaster.broadcastCommand("userPanel.refreshLogedUsers", ClientManagersStorage.findLogedUsersLogins());
    }           
    
 
    @Remote
    public JSONObject getUsersData(){                 
        try{         
           List<String>  logedUsers =  ClientManagersStorage.findLogedUsersLogins();
           return new JSONObject().add("user", getUser())
                   .add("onlineCount", ClientManagersStorage.getClientManagersCount())
                   .add("registeredCount", userManager.getUsersCount()) 
                   .add("logedUsersLogins",logedUsers) 
                   ;
        }catch(Throwable th){log.debug("in checkUser: "+th);}
        return null;
    }    
    
    @Remote
    public Integer getUsersCount(){        
        return userManager.getUsersCount();
    } 
    
    
    @Remote
    public void testForSpeed(){        
        ;
    }     
}
