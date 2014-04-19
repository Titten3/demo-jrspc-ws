/** websocket connector for jsrpc*/

/** use AJAWS instead AJAX since 09 oct 2013 */

var Server = {
	online : false
};

(function() {
	var connector = {
		connected : false
	};
	
	var p = function(s){try{console.log(s);}catch(any){}};

	connector.socket = null;

	connector.connect = function(url) {		
		if ('WebSocket' in window) {
			connector.socket = new WebSocket(url);
		} else if ('MozWebSocket' in window) {
			connector.socket = new MozWebSocket(url);
		} else {
			connector.disabled = true;
			alert('This browser not support WebSockets!');
			return;
		}
		connector.enabled = true;

		connector.socket.onopen = function() {
			log('WebSocket connection open.');
			connector.connected = true;
			Listeners.notify("onConnect");			
		};

		connector.socket.onclose = function() {			
			log('WebSocket closed.');
			connector.connected = false;
			Listeners.notify("onDisconnect");
		};		

		connector.socket.onmessage = function(message) {
			var data = message.data;
			//
			log("socket.onmessage: data="+data);
			var response = JSON.parse(data);
			// 	log("socket.onmessage: response="+data);
			var requestId = response.requestId;
			if (requestId) {/** server return response */					
				var control = Server.socketRequests["request_" + requestId].control;
				if (control) {control.disabled = false;}									
				if (response.error) {
					//
					log("socketReturn: response.error=" + response.error);
					var errorCallback = Server.socketRequests["request_" + requestId].errorCallback;
					log("socketReturn: errorCallback=" + errorCallback);
					if (errorCallback) {
						try {
							errorCallback(response.error);
						} catch (ex) {
							error("in connector.socket.onmessage errorCallback: " + ex + ", data=" + data);
						}
					}else{
						error(response.error);
					}
				} else {	
				    //p("socketReturn: response=" + JSON.stringify(response));
					var successCallback = Server.socketRequests["request_" + requestId].successCallback;
					if (successCallback) {
						try {
							successCallback(response.result);
						} catch (ex) {
							error("in connector.socket.onmessage successCallback: " + ex + ", data=" + data);
						}
					}
				}
				delete Server.socketRequests["request_" + requestId];
			} else {
				/** server call client or broadcast */
				var method = eval(response.method);
				// log("method="+method);
				var params = response.params;
				// log("params="+params);
				try {
					method(params);
					// eval(method+"(params);");
				} catch (ex) {
					error("in connector.socket.onmessage call method: " + ex + ", data=" + data);
				}
			}
		};
	};

	connector.initialize = function(url) {
		connector.url = url;
		try {
			connector.connect(url);
			return true;
		} catch (ex) {
			log("in connector.initialize: " + ex);
			return false;
		}
		;
	};

	Server.socketRequests = {};

	var requestId = 0;

	function sendSocket(service, method, params, successCallback, errorCallback, control) {
		if (!checkSocket()) {return;}
		requestId++;
		
		if(!params){params = [];}
		if(!isArray(params)){params = [params];}
		
		var data = {
			service : service,
			method : method,
			params : params,
			requestId : requestId
		};
		Server.socketRequests["request_" + requestId] = {
			successCallback : successCallback,
			errorCallback : errorCallback,
			control : control
		};

		if (control) {control.disabled = true;}
	
		var message = JSON.stringify(data);
		log("sendSocket: "+message);
		connector.socket.send(message);
	}


	function checkSocket() {
		if (!connector.connected) {
			var connected = connector.initialize();
			if (!connected) {
				if(confirm('No socket connection. Try reload page.')){document.location.reload();}
			}
			return connected;
		}
		return true;
	}
	
	//Server.onUnload = function () {try {connector.socket.close();} catch (ignored) {}}

	Server.call = sendSocket;
	Server.initialize = connector.initialize;

})();



//function onUnload() {Server.onUnload();}
