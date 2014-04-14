
var root = null;

function rootController($scope){
		
	var self = $scope;	
    root = self;  
    self.loged = false;
    self.connected = false;
    
    Listeners.add("onDisconnect", function(){self.connected = false; self.$digest();});
    Listeners.add("onConnect", function(){self.connected = true; self.$digest();});
    
    HttpSessionInitializer.init();    
    
	
    
    
}


function error(s){if(window.console){console.error(s);}};
function log(s){if(window.console){console.log(s);}};
function nop(){}