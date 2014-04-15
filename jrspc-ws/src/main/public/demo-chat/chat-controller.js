
/** no messages preload */

var chatPanel = null;

function chatController($scope){
		
	var self = $scope;	
    chatPanel = self;
    self.messages = [/*{to: "1", from: "u1", text: "aga1", clientTime: 123456789, serverTime: 123456989}, 
                     {to: "all", from: "user2", text: "aga2", clientTime: 123457189, serverTime: 123457989}*/];
    self.newMessage = "";
    self.sendPrivate = false;
    self.privateTo = "";
    self.showPrivateOnly = false;
    self.error = "";

    
    self.$watch('sendPrivate', function(){
    	log("sendPrivate="+self.sendPrivate);
    	if(self.sendPrivate && self.privateTo == ""){
    		self.error = "Click by name for select one!";    		
    		self.sendPrivate = false;
    	}else if(self.sendPrivate && self.privateTo == "chat robot"){
    		self.error = "Select any other name!";
    		self.sendPrivate = false;
    	}    	    	
    });
    

    self.selectUser = function(login){    	
    	if(login == "chat robot"){self.error = "You cannot talk with robot!"; 	return;}        
    	if(login == userPanel.user.login){self.error = "You cannot talk with yourself!";	return;}
    	self.privateTo = login;
    	self.sendPrivate = true;
    }
    
    
    self.sendMessage = function(command){
    	if(self.newMessage.length == 0){
    		self.error = "Please, enter message!";
    		return;
    	}
    	var message = {to: (self.sendPrivate ? self.privateTo : "all"), from: userPanel.user.login, text: self.newMessage, clientTime: new Date().getTime()};
    	
    	Server.call("chatService", "dispatchMessage", message,
    	function(){self.newMessage = ""; 
    	//self.onChatMessage(message, true);    	
    	self.$digest();}, function(error){
    		
    		self.onError(error);}, command);
    	return true;
    }
    
    self.checkSend = function(event){
    	var code = event.keyCode || event.which;
        if ((code === 13) && (event.shiftKey)) {
        	 event.preventDefault();
        	 self.sendMessage(sendCommand);
        }         
    }
    
               
    /** called from server */
    self.onChatMessage = function (message, fromClient){
    	//message.isMy = message.from == userPanel.user.login;    	
    	message.isPrivate = (message.to != "all");
    	self.messages.push(message);
    	if(!fromClient){self.$digest();}		
    	chatConsole.scrollTop = chatConsole.clientHeight + chatConsole.scrollHeight;    	
    }
        
    //Listeners.add("onConnect", function(){self.loadMessages();});
    
    
}




