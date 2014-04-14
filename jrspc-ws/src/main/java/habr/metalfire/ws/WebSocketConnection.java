package habr.metalfire.ws;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


final class WebSocketConnection extends MessageInbound implements Serializable{

    private static final long serialVersionUID = 596127588345365090L;

    protected Log log = LogFactory.getLog(WebSocketConnection.class);

    private final ClientManager clientManager;


    public WebSocketConnection(ClientManager clientManager) {
        this.clientManager = clientManager;        
    }

    @Override
    protected void onOpen(WsOutbound outbound) {
        if(clientManager != null){
           clientManager.addConnection(this);
        } /** otherwise - client is bot */       
    }
    

    @Override
    protected void onTextMessage(CharBuffer message) throws IOException {
        try {                   
            String connectionId = String.valueOf(this.hashCode());
            String request = message.toString();
            clientManager.handleClientRequest(request, connectionId);            
        } catch (Throwable th) {
            log.error("in onTextMessage: " + th);
        }
    }

    @Override
    protected void onClose(int status) {
        if(clientManager != null){
            clientManager.removeConnection(this);
        }        
    }

    @Override
    protected void onBinaryMessage(ByteBuffer buffer) throws IOException {
        log.info("onBinaryMessage: " + buffer.capacity());
    }

 

}