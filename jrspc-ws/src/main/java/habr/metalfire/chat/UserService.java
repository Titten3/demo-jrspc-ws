package habr.metalfire.chat;

import habr.metalfire.jrspc.AbstractService;
import habr.metalfire.jrspc.Remote;
import habr.metalfire.jrspc.Secured;
import habr.metalfire.jrspc.User;
import habr.metalfire.jrspc.UserManager;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class UserService extends AbstractService {

    private static final long serialVersionUID = 1L;

    @Autowired
    UserManager userManager;    
    
    @Autowired
    private HttpSession session;
               
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
         //User remoteUser
         //String login = remoteUser.getLogin(), password  = remoteUser.getPassword();         
        //
        log.debug("logIn: login="+login+", password="+password);
         String error = "Unknown combination of login and password!";
         User user = userManager.findByLogin(login);
          log.debug("logIn: user="+JSONObject.fromObject(user));
         if(user == null){ throw new RuntimeException(error);}
         if(!user.getPassword().equals(password)){ throw new RuntimeException(error);} 
         session.setAttribute("user", user);
         
         return user;
    }     
    

    @Secured("User") 
    @Remote
    public void logOut(){       
         session.removeAttribute("user");
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
           return (User) session.getAttribute("user");
        }catch(Throwable th){log.debug("in checkUser: "+th);}
        return null;
    }    
    
}
