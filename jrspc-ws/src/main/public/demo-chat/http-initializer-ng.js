/** use if not ws_connector.js applyed only! */
var appName = "jrspc-ws"; 
var secured = document.location.protocol == "https:" ? "s" : "";
var HttpSessionInitializer = {url: "http"+secured+"://"+ document.location.host +"/"+appName+"/init"};

/** called from root-controller.js after its initialization */
HttpSessionInitializer.init = function($http) {	        	
    	$http.post(this.url, "").success(function(response){    		 
    		try {
				if (response.error) {
					error(response.error);
				} else {					
					loged = response.loged;					
					Server.initialize("ws"+secured+"://"+ document.location.host +"/"+appName+"/ws?clientManagerId="+response.clientManagerId);
					if(loged){Listeners.notify("onLogin");}					
				}
			} catch (conectionError) {
				error("in HttpSessionInitializer.init: " + conectionError);
			}	    		
			 log("session initialized with manager: "+response.clientManagerId);   
    	}).error(function() {error("network error!");});    	    	    	
}	


var Listeners = {};
(function() {
	var listeners = {};

	function it(array, f){
	    for(var i = 0; i < array.length; i ++){f(array[i], i);}
    }	 
	
	function add(eventName, f) {	
		
		if (!listeners[eventName]) {
			listeners[eventName] = [];
		}
		var number = listeners[eventName].length;
		listeners[eventName][number] = f;
		// st.p("added: eventName="+eventName+", f="+f.name);
		return {
			eventName : eventName,
			number : number
		};
	}
	
	function clear(eventName) {
		if (!listeners[eventName]) {
			return;
		}
		delete listeners[eventName];		
	}
	
	function notify(eventName, data) {
		if (!listeners[eventName]) {
			return;
		}
		it(listeners[eventName], function(f) {
			try {
				f(data);
				// st.p("notified: eventName="+eventName+", f="+f.name);
			} catch (ex) {
				error("in notifylisteners: " + ex.message);
			}
		});
	}

	Listeners.add = add;
	Listeners.clear = clear;
	Listeners.notify = notify;


})();






