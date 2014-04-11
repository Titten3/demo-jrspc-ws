


function userController($scope){
		
	var self = $scope;	

	self.user = {login: "", password: ""};
	
	self.error = "";
	self.result = "Для входа или регистрации - введите логин и пароль.";
	self.loged = false;

	
	/** This method will called at application initialization (see last string in this file). */
	
	self.trySetSessionUser = function(control){
		Server.call("testUserService", "getSessionUser", null, 
		   function(user){
			log("checkUser: user="+JSON.stringify(user));
			if(!user.id){return;}
			self.user = user;
			self.loged = true;
			self.$digest();			
		}, self.onError, control);		
	}	
	
	
	/** common user methods */
	
	self.registerUser = function(control){
		Server.call("testUserService", "registerUser", self.user, 
		   function(id){
			self.user.id = id;			
			self.onSuccess("you registered with id: "+id);		
			setTimeout(function(){control.disabled = true;}, 20);
		}, self.onError, control);		
	}
	
	self.logIn = function(control){//[self.user] , self.user.password, true
		self.loginControl = control;
		Server.call("testUserService", "logIn", [self.user.login, self.user.password], function(user){
			self.user = user;
			self.loged = true;
			self.onSuccess("you loged in with role: "+user.role);	
			setTimeout(function(){control.disabled = true;}, 20);
		}, self.onError, control);		
	}
	
	
	self.logOut = function(control){		
		Server.call("testUserService", "logOut", {}, function(){
			self.user.role = "";
			self.user.city = "";
			self.loged = false;
			self.onSuccess("you loged out");
			setTimeout(function(){
				control.disabled = true;
				if(self.loginControl){self.loginControl.disabled = false;}
			}, 20);
		}, self.onError, control);	
	}		
	
	self.getUsersCount = function(control){
		Server.call("testAdminService", "getUsersCount", null, function(count){
			self.onSuccess("users count: "+count);			
		}, self.onError, control);			
	}	
	
	self.changeCity = function(control){
		Server.call("testUserService", "changeCity", self.user.city, function(){
			self.onSuccess("users city changed to: "+self.user.city);			
		}, self.onError, control);			
	}		
	
	
	/** admin methods */
	
	self.grantRole = function(control){		//
		Server.call("testAdminService", "grantRole", 
			[self.userId, self.role, true, [{id:2, login:"qwer"},{id:3, login:"333"} ], {id:4, login:"555"}], function(result){
     		self.onSuccess(result);		
		}, self.onError, control);		
	}	
		
	self.removeUser = function(control){
		Server.call("testAdminService", "removeUser", {userId: self.userId}, self.onSuccess, self.onError, control);		
	}	
	
	/** test methods */
	
	
	self.testCallNotExistedService = function(control){		
		Server.call("notExistedService", "anyMethod", null, 
				alert, alert, control);		
	}	
	
	
	self.testCallPublicMethod = function(control){		
		Server.call("testCallService", "testPublicMethod", null,
				alert, alert, control);		
	}	
	
	
	self.testCallUserMethod = function(control){		
		Server.call("testCallService", "testUserMethod", null, 
				alert, alert, control);		
	}	
	
	self.testCallAdminMethod = function(control){		
		Server.call("testCallService", "testAdminMethod", null, 
				alert, alert, control);		
	}		
		
	
	self.testArrayArguments = function(control){		
		Server.call("testCallService", "testArrayArguments", 
			[self.userId, self.role, true, [{id:1, login:"111"},
			 {id:2, login:"222"} ], {id:3, login:"333"}],
			 alert, alert, control);		
	}	
	
	self.testObjectArgument = function(control){		
		Server.call("testCallService", "testObjectArgument", 
			{id:2, login:"222"}, alert, alert, control);		
	}	
	
	self.testJSONObjectArgument = function(control){		
		Server.call("testCallService", "testJSONObjectArgument", 
			{id:2, login:"222"}, alert, alert, control);		
	}		
	
	self.testPrimitiveArgument = function(control){		
		Server.call("testCallService", "testPrimitiveArgument", "222", 
				alert, alert, control);		
	}
	
	self.testPrimitivesListArgument = function(control){		
		Server.call("testCallService", "testPrimitivesListArgument", 
			[111, 222, 333], alert, alert, control);		
	}	
	
	self.testPrimitivesArrayArgument = function(control){		
		Server.call("testCallService", "testPrimitivesArrayArgument", 
			[111, 222, 333], alert, alert, control);		
	}	
	
	self.testObjectsListArgument = function(control){		
		Server.call("testCallService", "testObjectsListArgument", 
			[{id:1, login:"111"}, {id:2, login:"222"}], 
			alert, alert, control);		
	}	
	
	self.testObjectsArrayArgument = function(control){		
		Server.call("testCallService", "testObjectsArrayArgument", 
			[{id:1, login:"111"}, {id:2, login:"222"}], 
			alert, alert, control);		
	}
	
	self.testObjectsArguments = function(control){		
		Server.call("testCallService", "testObjectsArguments", 
			[{id:1, login:"111"}, {id:2, login:"222"}], 
			alert, alert, control);		
	}	
	
	self.testVoidReturn = function(control){		
		Server.call("testCallService", "testVoidReturn",null, 
			function(){alert("void returned");}, alert, control);		
	}		
	
	self.testPrimitiveReturn = function(control){		
		Server.call("testCallService", "testPrimitiveReturn",null, 
			alert, alert, control);		
	}		
	
	self.testObjectReturn = function(control){		
		Server.call("testCallService", "testObjectReturn", null, 
			function(user){alert("user.city="+user.city);}, alert, control);		
	}			
	
	self.getTestCode = function(functionName){
		return ""+eval(functionName);
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
	
	
	/** initialization */
	self.trySetSessionUser();
}

