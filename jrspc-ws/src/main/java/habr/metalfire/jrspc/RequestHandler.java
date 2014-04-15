package habr.metalfire.jrspc;

import habr.metalfire.ws.ClientManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RequestHandler {

    final static Log log = LogFactory.getLog(RequestHandler.class);

    @Autowired
    private ApplicationContext applicationContext;


    public RequestHandler() { }

    public JSONObject handleRequest(String requestJson, ClientManager clientManager) {
        //
        log.debug("requestJson=" + requestJson);

        JSONObject request = JSONObject.fromObject(requestJson);
        
        String serviceName = request.optString("service");
        String methodName = request.optString("method");
        Object paramsObject = request.opt("params");
        
        Long requestId = request.optLong("requestId", -1L);
        
        if (paramsObject == null) {
            paramsObject = new JSONArray();
        }
        JSONArray paramsArray = null;
        if (paramsObject instanceof JSONArray) {
            paramsArray = (JSONArray) paramsObject;
        } else {
            paramsArray = new JSONArray();
            paramsArray.add(paramsObject);
        }
        log.debug("request =" + request);
        JSONObject response = callServiceMethod(serviceName, methodName, paramsArray, clientManager);
        response.put("requestId", requestId);       
        log.debug("response=" + response);
        return response;
    }

    private JSONObject callServiceMethod(String serviceName, String methodName, JSONArray argumentsList, ClientManager clientManager) {
        JSONObject response = new JSONObject();
        try {
            
            Object serviceObject = null;
            try{
                serviceObject = applicationContext.getBean(serviceName);
            }catch(Throwable th){
                throw new RuntimeException("AbstractService bean with name " + serviceName + " cannot be instantiated!");
            }
            
            if (serviceObject == null) {
                throw new RuntimeException("AbstractService bean with name " + serviceName + " not found!");
            }
            if (!(serviceObject instanceof AbstractService)) {
                throw new RuntimeException("Collable service \"" + serviceName + "\" MUST inherit AbstractService! ");
            }
            AbstractService service = (AbstractService) serviceObject;
            service.setClientManager(clientManager); 
            Object result = invokeMethod(service, methodName, argumentsList);
            if (result == null) {
                result = new JSONObject();
            }
            response.put("result", result);
        } catch (Throwable th) {
            response.put("error", th.getMessage());
            //log.error(new ThrowableWriter(th).toString());
        }
        return response;
    }

    private Object invokeMethod(AbstractService service, String methodName, JSONArray argumentsList) throws Throwable {
        try {
            User user = service.getUser();
            log.debug("user=" + JSONObject.fromObject(user));
            Class<?> ownerClass = service.getClass();

            Method actionMethod = findMethodByName(ownerClass, methodName);
            Class<?>[] parameterTypes = actionMethod.getParameterTypes();
            Object[] arguments = parseMethodArguments(parameterTypes, argumentsList);

            checkMethodAccess(actionMethod, user);
            Object result = actionMethod.invoke(service, arguments);
            return result == null ? new Object() : result;
        } catch (Throwable th) {
            if (th instanceof InvocationTargetException) {
                th = ((InvocationTargetException) th).getTargetException();
            }
            if (th instanceof NoSuchMethodException) {
                th =
                        new RuntimeException("Method \"" + methodName + "\" not found on class \""
                                + service.getClass().getName() + "\"!");
            }
            throw th;
        }
    }

    private void checkMethodAccess(Method method, User user) {
        if (!method.isAnnotationPresent(Remote.class)) {
            throw new RuntimeException("Remotely invoked method MUST be annotated as Remote!");
        }
        if (method.isAnnotationPresent(Secured.class)) {
            String[] roles = method.getAnnotation(Secured.class).value();
            if (user == null || (!Arrays.asList(roles).contains(user.getRole()) && !"Admin".equals(user.getRole()))) {
                String message =
                        "User not in role: " + StringUtils.arrayToDelimitedString(roles, " or ")
                                + ", required for invocation of \"" + method.getName() + "\" method !";
                throw new RuntimeException(message);
            }
        }
    }

    private Method findMethodByName(Class<?> ownerClass, String methodName) {
        Method[] methods = ownerClass.getMethods();
        for (Method method : methods) {
            // log.debug("methodName="+method.getName());
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new RuntimeException("METHOD_NOT_FOUND, methodName=" + methodName);
    }

    public static void main(String[] a) {
        // Long x = 9L;
        log.debug("" + Long.class.isPrimitive());
    }

    private boolean isPrimitive(Class<?> paramClass) {
        /** log.debug(""+Long.class.isPrimitive()); output: false SIC! */
        return paramClass.equals(String.class) || paramClass.equals(Long.class) || paramClass.equals(long.class)
                || paramClass.equals(Double.class) || paramClass.equals(double.class)
                || paramClass.equals(Boolean.class) || paramClass.equals(boolean.class)
                || paramClass.equals(Integer.class) || paramClass.equals(int.class) || paramClass.equals(Float.class)
                || paramClass.equals(float.class) || paramClass.equals(Short.class) || paramClass.equals(short.class)
                || paramClass.equals(Byte.class) || paramClass.equals(byte.class) || paramClass.equals(Character.class)
                || paramClass.equals(char.class);
    }

    private Object converPrimitiveToType(Object primitive, Class<?> type) {
        try {
            // log.debug("converPrimitiveToType: toType="+toType.getName());
            if (type.equals(String.class)) {
                return "" + primitive;
            }
            String primitiveAsString = "" + primitive;
            if (type.equals(Long.class) || type.equals(long.class)) {
                return new Long(primitiveAsString);
            }
            if (type.equals(Double.class) || type.equals(double.class)) {
                return new Double(primitiveAsString);
            }
            if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return new Boolean(primitiveAsString);
            }
            if (type.equals(Integer.class) || type.equals(int.class)) {
                return new Integer(primitiveAsString);
            }
            if (type.equals(Float.class) || type.equals(float.class)) {
                return new Float(primitiveAsString);
            }
            if (type.equals(Short.class) || type.equals(short.class)) {
                return new Short(primitiveAsString);
            }
            if (type.equals(Byte.class) || type.equals(byte.class)) {
                return new Byte(primitiveAsString);
            }
            if (type.equals(Character.class) || type.equals(char.class)) {
                return primitiveAsString.length() > 0 ? new Character(' ') : new Character(primitiveAsString.charAt(0));
            }
        } catch (Throwable any) {
            log.error("in converPrimitiveToType: " + any);
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object[] parseMethodArguments(Class[] parametersTypes, JSONArray parametersValues) {
        Object[] arguments = new Object[parametersTypes.length];
        if (parametersTypes.length == 0) {
            return arguments;
        }
        //log.debug("parameterTypes[0]=" + parametersTypes[0].getName() + ", isArray=" + parametersTypes[0].isArray());
        if (parametersTypes.length == 1 && (parametersTypes[0].isArray() || parametersTypes[0].equals(List.class))) {
            if (parametersTypes[0].isArray()) {
                throw new RuntimeException("Arryas parameters not supported, use List instead!");
            } else {
                arguments[0] = parametersValues;
            }
            return arguments;
        }

        if (parametersValues.size() != parametersTypes.length) {
            throw new RuntimeException("request method parameters count(" + parametersValues.size()
                    + ") != server method parameters count(" + parametersTypes.length + ")!");
        }
        for (int i = 0; i < parametersTypes.length; i++) {
            Class parameterType = parametersTypes[i];
            if (parameterType.isArray()) {
                throw new RuntimeException("Arryas parameters not supported, use List instead!");
            }
            Object argument = parametersValues.get(i);
            //log.debug("parameterType=" + parameterType.getName());
            if (isPrimitive(parameterType)) {
                //log.debug("isPrimitive: " + argument + " " + argument.getClass().getName());
                if (!argument.getClass().equals(parameterType)) {
                    // throw new
                    // RuntimeException("Invalid type of "+i+" parameter in client request! Requried: "
                    // +parameterType.getName()+", Occured: "+argument.getClass().getName());
                    arguments[i] = converPrimitiveToType(argument, parameterType);
                } else {
                    arguments[i] = argument;
                }

            } else if (parameterType.equals(JSONObject.class)) {
                //log.debug("JSONObject: " + ((JSONObject) argument));
                arguments[i] = (JSONObject) argument;

            } else if (parameterType.equals(List.class)) {
                //log.debug("JSONArray: " + ((JSONArray) argument));
                arguments[i] = (JSONArray) argument;
            } else {
                //log.debug("Object: " + JSONObject.fromObject(argument));
                arguments[i] = JSONObject.toBean(JSONObject.fromObject(argument), parameterType);
            }
        }
        return arguments;
    }

}
