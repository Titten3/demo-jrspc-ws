package habr.metalfire.jrspc;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ThrowableWriter {
   
   StringWriter sw = new StringWriter();
   
   public ThrowableWriter(Throwable th) {     
      PrintWriter pw = new PrintWriter(sw);
      if(th != null){
        th.printStackTrace(pw);
      }
   }
   
   @Override
   public String toString(){
      String ret = "\n"+sw.getBuffer().toString();      
      return ret;
   }


   

}
