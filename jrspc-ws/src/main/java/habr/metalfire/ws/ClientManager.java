package habr.metalfire.ws;

import habr.metalfire.jrspc.RequestHandler;
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

    private static final long serialVersionUID = -4215032993101840538L;

    protected static Log log = LogFactory.getLog(ClientManager.class);

    private final Map<String, WebSocketConnection> connections = new HashMap<String, WebSocketConnection>();

    private HttpSession session;
    
    @Autowired
    private RequestHandler requestHandler;
        
    @Autowired
    private UserManager userManager;        

    
    private Long waitForReloadTime = 6000L;// 6 sec;
      
    
    public void setSession(HttpSession session) {
        /** session will be invalidated at connection removing */
        session.setMaxInactiveInterval(Integer.MAX_VALUE);//69.04204112011317 years
        this.session = session;
        new Thread(new Runnable() {            
            @Override
            public void run() {
                /** Giving time to client, for establish websocket connection. */
                try {Thread.sleep(60000);} catch (InterruptedException ignored) {}
                /** if client not connected via websocket until this time - it is bot */
                if (connections.size() == 0) {removeMe();}                                
            }            
        }).start();        
    }
    
    /** harakiri pattern */
    private void removeMe() {
        ClientManagersStorage.removeClientManager(this);   
    }
    
    public void addConnection(WebSocketConnection webSocketConnection) {
        String connectionId = getObjectHash(webSocketConnection);
        connections.put(connectionId, webSocketConnection);
        //log.debug("addConnection:  connections.size()=" + connections.size());
    }

    public void removeConnection(WebSocketConnection webSocketConnection) {
        String connectionId = getObjectHash(webSocketConnection);
        connections.remove(connectionId);
        if (connections.size() == 0) {
            //log.debug("removeConnection before wait:  connections.size()=" + connections.size());
            /** may be client just reload page? */
            try {Thread.sleep(waitForReloadTime);} catch (Throwable ignored) {}
            //log.debug("removeConnection after wait:  connections.size()=" + connections.size());                 
            if (connections.size() == 0) {
                /** no, client leave us (page closed in browser)*/      
                ClientManagersStorage.removeClientManager(this); 
                log.debug("client " + getId() + " disconnected");                    
            }
        }
    }    
       

   
    /** ненависть - обратная сторона страха */

    public String getId() {
        return String.valueOf(this.hashCode());
    }

    public void handleClientRequest(String request, String connectionId) {
        //log.debug("handleClientRequest request=" + request);
        //log.debug("handleClientRequest user=" + getUser());   
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
            sendCommandToClientConnection(connection, method, params);             
        }        
    }    

    private void sendCommandToClientConnection(WebSocketConnection connection, String method, Object params) {
        JSONObject commandBody = new JSONObject();
        if(params == null){params = new JSONObject();}
        commandBody.put("method", method);
        commandBody.put("params", params);        
        CharBuffer buffer = CharBuffer.wrap(commandBody.toString());
        //
        log.debug("sendCommandToClient: buffer=" + buffer.toString());
        //log.debug("sendCommandToClient: method=" + method+", params="+params);  
        try {
            connection.getWsOutbound().writeTextMessage(buffer);                     
        } catch (IOException ioe) {
            log.error("in sendCommandToClientConnection: in writeTextMessage: " + ioe);
        }                
    }
       
    
    public void removeSessionVariable(String key) {
       session.removeAttribute(key);        
    }
     
    public void putSessionVariable(String key, Object value) {
        session.setAttribute(key, value);        
        //log.debug("putSessionVariable: sessionStorage="+sessionStorage);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getSessionVariable(String key) {
        //log.debug("getSessionVariable: sessionStorage="+sessionStorage);
        if(session == null){return null;}
        return (T) session.getAttribute(key);        
    }


    private static String getObjectHash(Object object) {                
        return String.valueOf(object.hashCode());
    }

    public HttpSession getSession() {
        return session;
    }




}
