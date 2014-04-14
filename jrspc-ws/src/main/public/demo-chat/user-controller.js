
var userPanelController = null;

function userController($scope){
		
	var self = $scope;	
    userPanelController = self;
	self.user = {login: "", password: ""};
	
	self.error = "";
	self.result = "Для входа или регистрации - введите логин и пароль.";
	//self.loged = false;
	
	/** This method will called at application initialization (see last string in this file). */
	
	self.trySetSessionUser = function(control){
		Server.call("userService", "getSessionUser", null, 
		   function(user){
			log("checkUser: user="+JSON.stringify(user));
			if(!user.id){return;}
			self.user = user;
			root.loged = true;
			self.result = "You loged in with role: "+user.role;
			self.$digest();			
			root.$digest();		
		}, self.onError, control);		
	}	
	
	
	/** common user methods */
	
	self.registerUser = function(control){
		Server.call("userService", "registerUser", self.user, 
		   function(id){
			self.user.id = id;			
			self.onSuccess("You registered with id: "+id);		
			setTimeout(function(){
				control.disabled = true;
				self.logIn(control);			
			}, 20);
		}, self.onError, control);		
	}
	
	self.logIn = function(control){//[self.user] , self.user.password, true
		self.loginControl = control;
		Server.call("userService", "logIn", [self.user.login, self.user.password], function(user){
			self.user = user;
			root.loged = true;
			root.$digest();	
			self.onSuccess("You loged in with role: "+user.role);	
			setTimeout(function(){control.disabled = true;}, 20);
		}, self.onError, control);		
	}
	
	
	self.logOut = function(control){		
		Server.call("userService", "logOut", {}, function(){
			self.user.role = "";
			self.user.city = "";
			root.loged = false;
			root.$digest();	
			self.onSuccess("You loged out");
			setTimeout(function(){
				control.disabled = true;
				if(self.loginControl){self.loginControl.disabled = false;}
			}, 20);
		}, self.onError, control);	
	}		
	
	self.getUsersCount = function(control){
		Server.call("adminService", "getUsersCount", null, function(count){
			self.onSuccess("users count: "+count);			
		}, self.onError, control);			
	}	
	
	self.changeCity = function(control){
		Server.call("userService", "changeCity", self.user.city, function(){
			self.onSuccess("users city changed to: "+self.user.city);			
		}, self.onError, control);			
	}		
	

	
	/** common callbacks */
	
	self.onError = function(error){
		self.error = error;		
		self.$digest();		
	}
	
	self.onSuccess = function(result){	
		self.result = result;
		self.error = "";
		self.$digest();		
	}		
	
	
	/** user initialization */
	Listeners.add("onConnect", function(){self.trySetSessionUser();});
	
}

