package com.mcg.batch.mbeans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.core.support.threading.SmartBatchExecutor;
import com.mcg.batch.utils.PropertiesConfiguration;

public class SmartBatchRemoteAdmin implements DynamicMBean {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(SmartBatchRemoteAdmin.class);

    private static Map<String, Object> attributes = new HashMap<String, Object>();

    private static final ConcurrentHashMap<String, Method> ADMIN_METHODS = new ConcurrentHashMap<String, Method>();

    public Map<String, Object> getAttributes() {
	refresh();
	return attributes;
    }

    public SmartBatchRemoteAdmin() {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("SmartBatchRemoteAdmin.SmartBatchRemoteAdmin Started");
	}
	try {
		Method[] methods = SmartBatchRemoteAdmin.class.getDeclaredMethods();
		StringBuilder builder = new StringBuilder();

		for (Method method : methods) {
		    builder.setLength(0);
		    if (Modifier.isStatic(method.getModifiers())
			    || Modifier.isPrivate(method.getModifiers())) {
			continue;
		    }
		    builder.append(method.getName());
		    Class<?>[] parameterTypes = method.getParameterTypes();
		    builder.append(parameterTypes.length);
		    for (Class<?> parameterType : parameterTypes) {
			builder.append(parameterType.getName());
		    }
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding the method with key " + builder.toString());
		    }
		    ADMIN_METHODS.put(builder.toString(), method);
		    refresh();
		}
	} catch (Exception e) {
	    LOGGER.error("Could not initialize SmartBatch Remote Admin MBean due to exception", e);
	} finally {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("SmartBatchRemoteAdmin.SmartBatchRemoteAdmin Finished");
	    }
	}
    }

    private void refresh() {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("SmartBatchAdminMBean.refresh Started");
	}
	try {
	    attributes.put("NodeName", BatchConfiguration.NODE_NAME);
	    attributes.put("Environment", BatchConfiguration.BATCH_ENVIRONMENT);
	    attributes.put("ThreadKeepAliveTime",
		    BatchConfiguration.THREAD_POOL_KEEP_ALIVE_MS);
	    attributes.put("PoolStats",
		    SmartBatchExecutor.getSmartBatchThreadDetails());
	} catch (Exception e) {
	    LOGGER.error("Exception while refreshing attributes : ", e);
	} finally {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("SmartBatchAdminMBean.refresh Finished");
	    }
	}
    }

    public boolean refreshCustomConfiguration() {
	try {
	    PropertiesConfiguration.reinit();
	    return true;
	} catch (Exception e) {
	    LOGGER.error("Could not reload the Custom Configuration : ", e);
	    return false;
	}
    }

    @Override
    public Object getAttribute(String name) throws AttributeNotFoundException,
	    MBeanException, ReflectionException {
	// TODO Auto-generated method stub
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Attribute Name Requested : " + name);
	    }
	    refresh();
	if (name != null) {
	    if (attributes.get(name) == null) {
		throw new AttributeNotFoundException(
			"No Attribute found with name " + name);
	    }
	    return attributes.get(name);
	} else {
	    throw new ReflectionException(null, "Invalid Name specified");
	}
    }

    @Override
    public void setAttribute(Attribute attribute)
	    throws AttributeNotFoundException, InvalidAttributeValueException,
	    MBeanException, ReflectionException {
	// TODO Auto-generated method stub
	// Unsupported Operation.
    }

    @Override
    public AttributeList getAttributes(String[] names) {
	// TODO Auto-generated method stub
	AttributeList list = new AttributeList();
	refresh();
	if (names != null) {
	    for (String name : names) {
		Object value = attributes.get(name);
		if (value != null) {
		    list.add(new Attribute(name, value));
		}
	    }
	} else {
	    for (String name : attributes.keySet()) {
		Object value = attributes.get(name);
		if (value != null) {
		    list.add(new Attribute(name, value));
		}
	    }
	}
	return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
	    throws MBeanException, ReflectionException {
	// TODO Auto-generated method stub
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("SmartBatchRemoteAdmin.invoke() started ");
	}

	Method method = null;
	StringBuilder builder = null;
	try {
	    builder = new StringBuilder(actionName);
	    if (params != null) {
		builder.append(params.length);
		for (Object parameter : params) {
		    if (parameter instanceof Object[]) {
			builder.append("[L");
		    }
		    builder.append(parameter.getClass().getName());
		}
	    } else {
		builder.append(0);
	    }
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Retreving method " + builder.toString()
			+ " from cache");
	    }
	    method = ADMIN_METHODS.get(builder.toString());
	    return method.invoke(this, params);
	} catch (Exception e) {
	    throw new ReflectionException(e, "Unable to invoke the operation "
		    + actionName + " with the parameters provided");
	} finally {
	    builder = null;
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchRemoteAdmin.invoke() completed");
	    }
	}

    }

    @SuppressWarnings("finally")
    @Override
    public MBeanInfo getMBeanInfo() {
	// TODO Auto-generated method stub
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("SmartBatchRemoteAdmin.getMBeanInfo Started");
	}
        MBeanAttributeInfo[] attrs = null;
        MBeanOperationInfo[] opers = null;
	try {
	    attrs = new MBeanAttributeInfo[attributes.size()];
	    	int i = 0;
	        for (String name : attributes.keySet()) {
	            attrs[i] = new MBeanAttributeInfo(
	                    name,
	                    attributes.get(name).getClass().getName(),
	                    "Property " + name,
	                    true,   // isReadable
	                    false,   // isWritable
	                    false); // isIs
	            i++;
	        }
	        opers = new MBeanOperationInfo[1];
	        opers[0] = new MBeanOperationInfo(
	                    "refreshCustomConfiguration",
	                    "Reload custom properties from file",
	                    null,   // no parameters
	                    "void",
	                    MBeanOperationInfo.ACTION);
	} catch (Exception e) {
	    LOGGER.error("Could not get MBean Info ", e);
	} finally {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("SmartBatchRemoteAdmin.getMBeanInfo Finished");
	    }
	    return new MBeanInfo(
	                this.getClass().getName(),
	                "SmartBatch Remote Admin Bean",
	                attrs,
	                null,  // constructors
	                opers,
	                null); // notifications
	}
    }
}
