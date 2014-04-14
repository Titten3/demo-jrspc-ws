/** use if not ws_connector.js applyed only! */
var appName = "jrspc-ws"; 

var secured = document.location.protocol == "https:" ? "s" : "";

var HttpSessionInitializer = {url: "http"+secured+"://"+ document.location.host +"/"+appName+"/init"};

(function() {	
	function getXMLHttpRequest() {
		if (window.XMLHttpRequest) {
			return new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			return new ActiveXObject("Microsoft.XMLHTTP");
		}
		if(confirm("This browser not support AJAX!\nDownload modern browser?")){
			document.location = "http://www.mozilla.org/ru/firefox/new/";
		}else{
			alert("Download modern browser for work with this application!");
		    throw "This browser not suport Ajax!";
		}
	}		
	
    HttpSessionInitializer.init = function() {	
    	
		var request = getXMLHttpRequest();
		
		request.onreadystatechange = function() {			
			//log("request.status="+request.status+", request.readyState="+request.readyState+", responseText="+request.responseText);
			
			if ((request.readyState == 4 && request.status != 200)) {
				error("network error!");
				return;
			}	
			if (!(request.readyState == 4 && request.status == 200)) {return;}			
			//log("responseText="+request.responseText);
			
			try {
				var response = JSON.parse(request.responseText);
				if (response.error) {
					error(response.error);
				} else {
					loged = response.loged;
					initializeWebsocketConnector(response.clientManagerId);
					if(loged){Listeners.notify("onLogin");}
					
				}
			} catch (conectionError) {
				error("in init: " + conectionError);
			}			
		}
		log("Server.url="+HttpSessionInitializer.url);
		request.open("POST", HttpSessionInitializer.url, true);
		request.send("");
	}	
})();


function initializeWebsocketConnector(clientManagerId){
	Server.initialize("ws"+secured+"://"+ document.location.host +"/"+appName+"/ws", "clientManagerId="+clientManagerId);	
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






