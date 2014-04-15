package habr.metalfire.jrspc;

import habr.metalfire.ws.Broadcaster;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


@Component
public class UserManager implements Serializable{
        
    
    private static final long serialVersionUID = 1L;
    
    protected  Log log = LogFactory.getLog(UserManager.class);
    
    private HashMap<Long, User> idUsersMap = new HashMap<Long, User>();
    
    private HashMap<String, Long> loginIdMap = new HashMap<String, Long>();
      
    private AtomicLong nextId = new AtomicLong(0);

    @Value("${users.db.local}")   
    private String localUsersFile;
    
    @Value("${users.db.remote}")   
    private String remoteUsersFile;    
    
        
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
        appendUserToFile(user);
        Broadcaster.broadcastCommand("userPanel.setRegisteredCount", idUsersMap.size());
        return true;
    }

    public void updateUser(User user) {
       idUsersMap.put(user.getId(), user);       
    }

    public void removeUser(User user) {
       idUsersMap.remove(user.getId());       
       loginIdMap.remove(user.getLogin());         
       Broadcaster.broadcastCommand("userPanel.setRegisteredCount", idUsersMap.size());
    }
        
    public Integer getUsersCount() {
       return idUsersMap.size();  
    }    
    
    private boolean isLocal() {       
        return System.getProperty("user.dir").toLowerCase().startsWith("c:");
    }    
    
    private void appendUserToFile(User user){        
        File usersFile = new File(isLocal() ? localUsersFile : remoteUsersFile);          
            try {
                Files.append(JSONObject.stringify(user) +"\n", usersFile, Charsets.UTF_8);
            } catch (IOException e) {               
                log.error("in appendUserToFile: "+e);
            }              
    }
      
    
    @PostConstruct
    private void initUsers(){
        log.debug("initUsersData");
        File usersFile = new File(isLocal() ? localUsersFile : remoteUsersFile);    
        List<String> lines = null;
        try {
            lines = Files.readLines(usersFile, Charsets.UTF_8);
        } catch (IOException e) {
            log.error("in initUsersData: "+e);
            return;
        }
        for(String line: lines){
            User user = JSONObject.toBean(JSONObject.parse(line), User.class);
            idUsersMap.put(user.getId(), user);
            loginIdMap.put(user.getLogin(), user.getId());
        }   
    }

    
}
