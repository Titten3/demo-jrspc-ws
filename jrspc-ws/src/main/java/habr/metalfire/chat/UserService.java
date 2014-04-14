package habr.metalfire.chat;

import habr.metalfire.jrspc.AbstractService;
import habr.metalfire.jrspc.Remote;
import habr.metalfire.jrspc.Secured;
import habr.metalfire.jrspc.User;
import habr.metalfire.jrspc.UserManager;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UserService extends AbstractService {

    private static final long serialVersionUID = 1L;

    @Autowired
    UserManager userManager;    

               
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
         return user;
    }     
    

    @Secured("User") 
    @Remote
    public void logOut(){       
         getClientManager().removeSessionVariable("user");
    }           
    
    @Secured("User")   
    @Remote
    public void changeCity(String city){                  
        User user = getUser();
        user.setCity(city);                
        userManager.updateUser(user);
    }           
 
    @Remote
    public User getSessionUser(){           
        try{
           return getUser();
        }catch(Throwable th){log.debug("in checkUser: "+th);}
        return null;
    }    
    
}
