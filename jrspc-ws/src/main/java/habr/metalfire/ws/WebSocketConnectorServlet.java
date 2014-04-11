package habr.metalfire.ws;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class WebSocketConnectorServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	protected static Log log = LogFactory.getLog(WebSocketConnectorServlet.class);
	
	@Override
	protected StreamInbound createWebSocketInbound(String paramString, HttpServletRequest request) {
		String sessionManagerId = request.getParameter("sessionManagerId");		
		ClientManager sessionManager = ClientManagersStorage.findClientManager(sessionManagerId);
		if(sessionManager == null){
		    return new WebSocketConnection(null);
		}		
		log.debug("new connection");
		return new WebSocketConnection(sessionManager);
	}
	
}

