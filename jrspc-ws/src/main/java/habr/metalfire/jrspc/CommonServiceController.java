package habr.metalfire.jrspc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class CommonServiceController {

    final static Log log = LogFactory.getLog(CommonServiceController.class);


    
    @Autowired
    private RequestHandler requestHandler;
    
    
    @RequestMapping(value = "/jrspc-request", method = RequestMethod.POST)
    @ResponseBody
    private String processAjaxRequest(@RequestBody String requestJson) {
       return requestHandler.handleRequest(requestJson).toString();      
    }       


}