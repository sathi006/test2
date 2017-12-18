/**
 * 
 */
package com.mcg.batch.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ReflectionUtils {

    /**
     * prevent external instantiation
     */
    private ReflectionUtils() {
    }

    public static final Object doInvoke(final String methodName,
	    final Class<?> returnType, final String className, final Class<?>[] parameterTypes,
	    final Object... parameters) throws ReflectionInvokeExecption {
	try {
	    Object instance = Class.forName(className).newInstance();
	    Method method = Class.forName(className).getMethod(methodName, parameterTypes);
	    return (Object) doInvoke(method, returnType, instance, parameters);
	} catch (IllegalAccessException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (IllegalArgumentException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (NoSuchMethodException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (SecurityException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (ClassNotFoundException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (InstantiationException e) {
	    throw new ReflectionInvokeExecption(e);
	}
    }

    @SuppressWarnings("unchecked")
    public static final <T> T doInvoke(final Method method,
	    final Class<T> returnType, Object instance,
	    final Object... parameters) throws ReflectionInvokeExecption {

	try {
	    return (T) method.invoke(instance, parameters);
	} catch (IllegalAccessException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (IllegalArgumentException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (InvocationTargetException e) {
	    throw new ReflectionInvokeExecption(e);
	}

    }

    public static Object doStaticInvoke(String className, String methodName,
	    Object... parameters) throws ClassNotFoundException {
	return doStaticInvoke(Class.forName(className), methodName, parameters);

    }

    public static Object doStaticInvoke(Class<?> clazz, String methodName,
	    Object... parameters) {

	Class<?>[] paramTypes = null;
	Method method = null;
	Object result = null;
	try {
	    if (parameters != null && parameters.length > 0) {
		paramTypes = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
		    paramTypes[i] = parameters[i].getClass();
		}
	    }
	    if (paramTypes != null) {
		method = clazz.getDeclaredMethod(methodName, paramTypes);
	    } else {
		method = clazz.getDeclaredMethod(methodName);
	    }
	    if (Modifier.isStatic(method.getModifiers())) {
		method.setAccessible(true);
		if (paramTypes != null) {
		    result = method.invoke(null, parameters);
		} else {
		    result = method.invoke(null);
		}
		return result;
	    } else {
		throw new ReflectionInvokeExecption(
			"The method specified is not static...");
	    }
	} catch (NoSuchMethodException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (SecurityException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (IllegalAccessException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (IllegalArgumentException e) {
	    throw new ReflectionInvokeExecption(e);
	} catch (InvocationTargetException e) {
	    throw new ReflectionInvokeExecption(e);
	}
    }
}
