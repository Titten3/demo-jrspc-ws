package habr.metalfire.chat;

import habr.metalfire.jrspc.AbstractService;
import habr.metalfire.jrspc.Remote;
import habr.metalfire.jrspc.Secured;
import habr.metalfire.jrspc.User;
import habr.metalfire.jrspc.UserManager;
import habr.metalfire.ws.Broadcaster;

import java.util.Date;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatService extends AbstractService{

    private static final long serialVersionUID = 1L;
        
    @Autowired
    private UserManager userManager;
    
    @Secured("User")
    @Remote
    public void dispatchMessage(ChatMessage message){ 
        message.setServerTime(new Date().getTime());  
        String to = message.getTo();
        if("ALL".equalsIgnoreCase(to)){                   
            Broadcaster.broadcastCommand("chatPanel.onChatMessage", message);
            log.debug("broadcasted "+ JSONObject.fromObject(message));
        }else{            
            User fromUser = getUser();
            message.setFrom(fromUser.getLogin());
            User toUser = userManager.findByLogin(to);    
            if(toUser == null){throw new RuntimeException("User "+to+" not found!");}
            Long toUserId  = toUser.getId();    
            
            Broadcaster.sendCommandToUser(toUserId, "chatPanel.onChatMessage", message);        
            Broadcaster.sendCommandToUser(fromUser.getId(), "chatPanel.onChatMessage", message);     
            log.debug("private sent "+ JSONObject.fromObject(message));
        }                
    }                   
    
}
