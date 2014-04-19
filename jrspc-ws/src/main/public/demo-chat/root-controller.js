function error(s){if(window.console){console.error(s);}};
function log(s){if(window.console){console.log(s);}};
function isArray(object){try{return  JSON.stringify(object).substring(0,1) == "[";}catch(any){return false;}}
function now(){return new Date().getTime();};

var root = null;

function rootController($scope, $http){
	log("rootController started");	
	
	var self = $scope;	
    root = self;  
    self.loged = false;
    self.connected = false;   
    
    Listeners.add("onDisconnect", function(){self.connected = false; self.$digest();});
    Listeners.add("onConnect", function(){self.connected = true; self.$digest();});    
    
    HttpSessionInitializer.init($http);     
    log("rootController finished");       
}

/*
function testController($scope){	
	var self = $scope;	
   
    self.maxIterations = 1000;
    self.testIterations = self.maxIterations;
    self.testStart = 0;
    self.testEnd = 0;
    
    
    self.testForSpeedSerial = function(command){  
    	//with firebug: 4758, 4662, 4996    
    	//without firebug: 1299, 1113, 1054    	
    	if(self.testStart == 0){self.testStart = now();}
    	if(--self.testIterations <= 0){
    		var duration = now() - self.testStart;
    		//log
    		alert("testForSpeedSerial duration="+duration);    
    		self.testStart = 0;
    		self.testIterations = self.maxIterations;
    		return;
    	}
    	Server.call("userService", "testForSpeed", "", function(){ self.testForSpeedSerial(command); }, error, command);    	
    }
    
    self.testForSpeedParallelResponses = 0;
    
    self.testForSpeedParallel = function(command){    
    	//with firebug: 721 792 743 725 726
    	//without firebug: 616, 637, 632     	
    	self.testStart = now();    	
    	for(var i = 0; i < self.testIterations; i++){
    		Server.call("userService", "testForSpeed", "", 
    				function(){
    			       self.testForSpeedParallelResponses++ ; 
    			       if(self.testForSpeedParallelResponses >= self.maxIterations){
    			    	      	var duration = now() - self.testStart;
						    	//log
						    	alert("testForSpeedParallel duration="+duration);    		
						    	self.testForSpeedParallelResponses = 0;
    			       }
    				}, error, command); 
    	}
    	log("testForSpeedParallel sent "+self.testIterations+" in "+(now() - self.testStart)); 
    } 
}*/


