package com.mcg.batch.runtime.impl.batch.utils;

import static com.mcg.batch.utils.IDataUtils.convertToIData;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;

import com.mcg.batch.exceptions.RetryableException;
import com.mcg.batch.utils.PropertiesConfiguration;
import com.mcg.batch.utils.ServerDetailsDTO;
import com.mcg.batch.utils.StringHolder;
import com.mcg.batch.utils.ThreadContextUtils;
import com.wm.app.b2b.client.Context;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;

/**
 * The Class WmServiceInvokeHelper provides static method to invoke a webmethods
 * service using context invoke.
 */
public final class WmServiceInvokeHelper<I> {

    /** Constant LOGGER. */
    public static final Logger LOGGER = LoggerFactory
	    .getLogger(WmServiceInvokeHelper.class);

    /** Constant INPUT_PARAM_NAME. */
    public static final String INPUT_PARAM_NAME = "input";

    public static final String BATCH_NAME_PARAM = "batchName";

    public static final String EXEC_ID_PARAM_NAME = "executionId";

    public static final String INSTANCE_ID_PARAM_NAME = "instanceId";

    public static final String STEP_EXEC_ID_PARAM_NAME = "stepExecutionId";

    public static final String HEADER_PARAM_NAME = "header";

    public static final String CONTENT_PARAM_NAME = "content";

    /** Constant BATCH_EXEC_ID_PARAM_NAME. */
    public static final String BATCH_EXEC_ID_PARAM_NAME = "batchExecutionId";

    /** Constant OUTPUT_PARAM_NAME. */
    public static final String OUTPUT_PARAM_NAME = "output";

    /**
     * Invoke wm service.
     *
     * @param input
     *            the input
     * @param serviceName
     *            the service name
     * @param logicalServer
     *            the logical server
     * @return the object
     * @throws Exception
     *             the exception
     */
    public Object invokeWmService(Object input, final String serviceName,
	    final String logicalServer) throws Exception {

	String server = null, userName = null, password = null;
	Context context = null;
	ServerDetailsDTO serverDetail = null;

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("WmServiceInvokeHelper.invokeWmService Started");
	}
	try {
	    serverDetail = PropertiesConfiguration
		    .getServerDetails(logicalServer);
	    if (serverDetail == null) {
		throw new Exception("No Logical server definition"
			+ " found for alias " + logicalServer);
	    }
	    server = serverDetail.getHost() + ":" + serverDetail.getPort();
	    userName = serverDetail.getUsername();
	    password = EncryptorFactory.doTextDecryption(serverDetail
		    .getPassword());
	    context = new Context();
	    context.connect(server, userName, password);
	    IData in = IDataFactory.create();
	    IData translatorRequest = IDataFactory.create();
	    IData header = IDataFactory.create();
	    IData content = IDataFactory.create();
	    IDataCursor headerCursor = header.getCursor();
	    IDataCursor idc = in.getCursor();
	    IDataCursor contentCursor = content.getCursor();
	    IDataCursor requestCursor = translatorRequest.getCursor();
	    if (ThreadContextUtils.getJobExecution() != null) {
	    headerCursor.insertAfter(BATCH_NAME_PARAM, ThreadContextUtils
		    .getJobExecution().getJobInstance().getJobName());
	    headerCursor
		    .insertAfter(EXEC_ID_PARAM_NAME, Long
			    .toString(ThreadContextUtils.getJobExecutionId()));
	    headerCursor.insertAfter(
		    INSTANCE_ID_PARAM_NAME,
		    Long.toString(ThreadContextUtils.getJobInstanceID()));
	    List<StepExecution> steps = (List<StepExecution>) ThreadContextUtils
		    .getJobExecution().getStepExecutions();
	    if (steps != null && steps.size() != 0) {
		headerCursor.insertAfter(STEP_EXEC_ID_PARAM_NAME,
			Long.toString(steps.get(steps.size() - 1).getId()));
	    }
	    } else {
		throw new Exception("Could not retrieve execution information from context");
	    }
	    requestCursor.insertAfter(HEADER_PARAM_NAME, header);
	    headerCursor.destroy();
	    if (input instanceof String) {
		contentCursor.insertAfter("string", (String) input);
	    } else if (input instanceof StringHolder) {
		contentCursor.insertAfter("string",
			((StringHolder) input).getValue());
	    } else if (input instanceof Object) {
		contentCursor.insertAfter("document", convertToIData(input));
	    }
	    requestCursor.insertAfter(CONTENT_PARAM_NAME, content);
	    contentCursor.destroy();
	    idc.insertAfter("TranslatorRequest", translatorRequest);
	    requestCursor.destroy();
	    idc.destroy();
	    NSName service = NSName.create(serviceName);
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Invoking Wm service " + serviceName);
	    }
	    IData out = context.invoke(service, in);
	    if (out != null) {
		IDataCursor odc = out.getCursor();
		IData translatorResponse = IDataUtil.getIData(odc,
			"TranslatorResponse");
		odc.destroy();
		if (translatorResponse == null) {
		    throw new Exception(
			    "No response received even though the transformation service "
				    + serviceName + " was successful");
		}
		IDataCursor responseCursor = translatorResponse.getCursor();
		IData status = IDataUtil.getIData(responseCursor, "status");
		if (status != null) {
		    IDataCursor statusCursor = status.getCursor();
		    String serviceStatus = IDataUtil.getString(statusCursor,
			    "status");
		    String stringOutput = null;
		    Object objOutput = null;
		    if ("SUCCESS".equalsIgnoreCase(serviceStatus)) {
			IData opContent = IDataUtil.getIData(responseCursor,
				CONTENT_PARAM_NAME);
			IDataCursor opContentCursor = opContent.getCursor();
			stringOutput = IDataUtil.getString(opContentCursor,
				"string");
			objOutput = IDataUtil.get(opContentCursor, "object");
			if (stringOutput != null) {
			    if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Output received : " + stringOutput);
			    }
			    return new StringHolder(stringOutput);
			} else if (objOutput != null) {
			    if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Output received : " + objOutput);
			    }
			    return objOutput;
			} else {
			    throw new Exception(
				    "No response received even though the transformation service "
					    + serviceName + " was successful");
			}
		    } else {
			LOGGER.error("Invocation of service " + serviceName
				+ " failed due to exception : "
				+ IDataUtil.getString(statusCursor, "error"));
		    }
		} else {
		    throw new Exception(
			    "No response received even though the transformation service "
				    + serviceName + " was successful");
		}
	    }
	} catch (Exception e) {
	    throw new RetryableException("Invocation of service " + serviceName
		    + "Failed. ", e);
	} finally {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("WmServiceInvokeHelper.invokeWmService Finished");
	    }
	}
	return null;
    }
}
