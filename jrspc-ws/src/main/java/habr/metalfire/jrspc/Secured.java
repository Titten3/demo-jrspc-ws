package habr.metalfire.jrspc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** If method annotated as Secured  MethodInvoker throw exception,
 *  if User not in declared role.
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Secured {
    String[] value();    
}

