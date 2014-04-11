package habr.metalfire.jrspc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** If method NOT annotated as Remote MethodInvoker throw exception, 
 *  when user try to call this method from browser
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Remote {}
