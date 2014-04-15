package habr.metalfire.ws;

import habr.metalfire.chat.UserService;
import habr.metalfire.jrspc.RequestHandler;
import habr.metalfire.jrspc.User;
import habr.metalfire.jrspc.UserManager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("session")
public class ClientManager implements Serializable {

    private static final long serialVersionUID = 3556803133542452496L;

    protected static Log log = LogFactory.getLog(ClientManager.class);

    private final Map<String, WebSocketConnection> connections = new HashMap<String, WebSocketConnection>();

    private HttpSession session;
    
    private Map<String, Object> sessionStorage = new HashMap<String, Object>();

    @Autowired
    private RequestHandler requestHandler;
        
    @Autowired
    private UserManager userManager;        

    
    private Long waitForReloadTime = 6000L;// 6 sec;
             
    
    public void setSession(HttpSession session) {
        /** session will be invalidated at connection removing */
        session.setMaxInactiveInterval(Integer.MAX_VALUE);
        this.session = session;
        //this.sessionStorage = new HashMap<String, Object>();        
        log.debug("session ok");
        new Thread(new Runnable() {            
            @Override
            public void run() {
                try {
                /** Giving time to client, for establish websocket connection. */ 
                    Thread.sleep(60000);
                } catch (InterruptedException ignored) {}
                /** if client not connected via websocket until this time - it is bot */
                if (connections.size() == 0) {
                    invalidateSession();
                }                                
            }
        }).start();        
    }

    public void addConnection(WebSocketConnection webSocketConnection) {
        String connectionId = getObjectHash(webSocketConnection);
        connections.put(connectionId, webSocketConnection);
        log.debug("addConnection:  connections.size()=" + connections.size());
    }

    public void removeConnection(WebSocketConnection webSocketConnection) {
        String connectionId = getObjectHash(webSocketConnection);
        connections.remove(connectionId);
        if (connections.size() == 0) {
            log.debug("removeConnection before wait:  connections.size()=" + connections.size());
            try {
                /** may be client just reload page? */
                Thread.sleep(waitForReloadTime);
            } catch (Throwable ignored) {
            }
            log.debug("removeConnection after wait:  connections.size()=" + connections.size());
            if (connections.size() == 0) {
                /** no, client leave us (page closed in browser)*/                      
                invalidateSession();  
           
                log.debug("visitor " + getId() + " disconnected");                    
            }
        }
    }    
    
    
    /** called from timeout after setSession, if client not connected via websocket, 
     * or at websocket connection closing */
    private void invalidateSession(){              
        try {
            session.invalidate();
            User user = getUser();
            if(user != null){
                //setUser(null);
                Broadcaster.broadcastCommand("userPanel.setLogedCount", UserService.logedCount.decrementAndGet());           
            }                 
            ClientManagersStorage.removeClientManager(this); 
        } catch (Throwable th) {
            log.error("at session.invalidate: " + th);
        }
    }
   
    /** ненависть - обратная сторона страха */

    public String getId() {
        return String.valueOf(this.hashCode());
    }

    public void handleClientRequest(String request, String connectionId) {
        log.debug("handleClientRequest request=" + request);
        log.debug("handleClientRequest user=" + getUser());   
        /** handleRequest - never throws exceptions ! */
        JSONObject response = requestHandler.handleRequest(request, this);        
        String responseJson = response.toString();
        CharBuffer buffer = CharBuffer.wrap(responseJson);
        WebSocketConnection connection = connections.get(connectionId);
        try {
            connection.getWsOutbound().writeTextMessage(buffer);
        } catch (IOException ioe) {
            log.error("in handleClientRequest: in writeTextMessage: " + ioe);
        }
    }
    
    public <T> void  setUser(T user) {
        putSessionVariable("user", user);
    }

    public <T> T  getUser() {
        return getSessionVariable("user");
    }
        
    
    public void sendCommandToClient(String method, Object params) {
        for(WebSocketConnection connection: connections.values()){
            sendCommandToClient(connection, method, params);             
        }        
    }    

    public void sendCommandToClient(WebSocketConnection connection, String method) {
        sendCommandToClient(connection, method, new JSONObject());
    }

    private void sendCommandToClient(WebSocketConnection connection, String method, Object params) {
        JSONObject commandBody = new JSONObject();
        if(params == null){params = new JSONObject();}
        commandBody.put("method", method);
        commandBody.put("params", params);        
        CharBuffer buffer = CharBuffer.wrap(commandBody.toString());
        //log.debug("sendCommandToClient: buffer=" + buffer.toString());
        //log.debug("sendCommandToClient: method=" + method+", params="+params);  
        try {
            connection.getWsOutbound().writeTextMessage(buffer);                     
        } catch (IOException ioe) {
            log.error("in sendCommandToClient: in writeTextMessage: " + ioe);
        }                
    }
    
    public static void sendCommandToConnection(WebSocketConnection connection, String method) {
         sendCommandToConnection(connection, method, new Object());         
    }
    
    public static void sendCommandToConnection(WebSocketConnection connection, String method, Object params) {
        JSONObject commandBody = new JSONObject()
        .accumulate("method", method)
        .accumulate("params", params);
        CharBuffer buffer = CharBuffer.wrap(commandBody.toString());
        try {
            connection.getWsOutbound().writeTextMessage(buffer);
        } catch (IOException ioe) {
            log.error("in sendCommandToConnection: in writeTextMessage: " + ioe);
        }                
    }       
    
    public void removeSessionVariable(String key) {
       sessionStorage.remove(key);        
    }
     
    public void putSessionVariable(String key, Object value) {
        sessionStorage.put(key, value);        
        //log.debug("putSessionVariable: sessionStorage="+sessionStorage);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getSessionVariable(String key) {
        //log.debug("getSessionVariable: sessionStorage="+sessionStorage);
        if(sessionStorage == null){return null;}
        return (T) sessionStorage.get(key);        
    }


    private static String getObjectHash(Object object) {                
        return String.valueOf(object.hashCode());
    }




}
