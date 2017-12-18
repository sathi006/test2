package com.mcg.batch.utils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;

public class IDataUtils {

    public static final IData convertToIData(Object input) throws Exception {
	String paramName = null;
	Object methodResult = null;
	Method method = null;
	IData output = null;
	IDataCursor outputCursor = null;
	if (input != null) {
	    try {
		output = IDataFactory.create();
		outputCursor = output.getCursor();
		for (PropertyDescriptor propertyDescriptor : Introspector
			.getBeanInfo(input.getClass()).getPropertyDescriptors()) {
		    method = propertyDescriptor.getReadMethod();
		    if (method != null) {
			if (method.getName().startsWith("get")
				&& !method.getName().equalsIgnoreCase(
					"getClass")) {
			    paramName = propertyDescriptor.getDisplayName();
			    method.setAccessible(true);
			    methodResult = method
				    .invoke(input, (Object[]) null);
			    if (methodResult != null && paramName != null) {
				if (isJavaLang(methodResult)) {
				    outputCursor.insertAfter(paramName,
					    methodResult);
				} else {
				    outputCursor.insertAfter(paramName,
					    convertToIData(methodResult));
				}
			    }
			}
			paramName = null;
			methodResult = null;
			method = null;
		    }
		}
	    } finally {
		outputCursor.destroy();
	    }
	}
	return output;
    }

    public static boolean isJavaLang(Object check) {
	return check.getClass().getName().startsWith("java.lang");
    }
}
