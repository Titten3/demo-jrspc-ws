package habr.metalfire.ws;




public class Broadcaster{	    
    
    
	public static void broadcastCommand(String method) {	    
	    broadcastCommand(method, null);	    	    
	}

	
    public static void broadcastCommand(String method, Object params) {
        for (ClientManager sessionManager : ClientManagersStorage.clientManagers.values()) {
            sessionManager.sendCommandToClient(method, params);
        }
    }
    

    public static void sendCommandToUser(Long userId, String method, Object params) {
        ClientManager userSessionManager = ClientManagersStorage.findUserClientManager(userId);
        if(userSessionManager != null){
           userSessionManager.sendCommandToClient(method, params);
        }       
    }
	
}
