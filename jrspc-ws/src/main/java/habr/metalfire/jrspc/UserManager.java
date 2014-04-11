package habr.metalfire.jrspc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;


@Component
public class UserManager implements Serializable{
        
    private static final long serialVersionUID = 1L;
    
    
    private HashMap<Long, User> idUsersMap = new HashMap<Long, User>();
    
    private HashMap<String, Long> loginIdMap = new HashMap<String, Long>();
      
    private AtomicLong nextId = new AtomicLong(0);
        
    public User findById(Long id) {    
        if(id == null){throw new RuntimeException("UserManager: in findById: id == null!");}
        return idUsersMap.get(id);
    }
 
    public User findByLogin(String login) {
        Long id = loginIdMap.get(login);
        if(id == null){return null;}
        return  findById(id);
    }  
    
    public boolean saveUser(User user) {
        user.setId(nextId.addAndGet(1));
        idUsersMap.put(user.getId(), user);
        loginIdMap.put(user.getLogin(), user.getId());
        return false;
    }

    public void updateUser(User user) {
       idUsersMap.put(user.getId(), user);       
    }

    public void removeUser(User user) {
       idUsersMap.remove(user.getId());       
       loginIdMap.remove(user.getLogin());           
    }
        
    public Integer getUsersCount() {
       return idUsersMap.size();  
    }    
    
}
