/**
 * 
 */
package com.mcg.batch.utils;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class XMLUtils {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(XMLUtils.class);

	private static final ConcurrentHashMap<String, Templates> CACHED_TEMPLATES = new ConcurrentHashMap<String, Templates>();
	private static final ConcurrentHashMap<String, Long> CACHED_TEMPLATES_CHECK = new ConcurrentHashMap<String, Long>();

	/**
	 * prevent external instantiation
	 */
	private XMLUtils() {
	}

	public static final String prettyFormat(final String input, final int indent)
			throws TransformerException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("XMLUtils.prettyFormat() started");
		}
		Source xmlInput = null;
		StringWriter stringWriter = null;
		StreamResult xmlOutput = null;
		TransformerFactory transformerFactory = null;
		try {
			xmlInput = new StreamSource(new StringReader(input));
			stringWriter = new StringWriter();
			xmlOutput = new StreamResult(stringWriter);
			transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} finally {
			xmlInput = null;
			stringWriter = null;
			xmlOutput = null;
			transformerFactory = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("XMLUtils.prettyFormat() completed");
			}
		}
	}

	/**
	 * Private method to load the xsl file.<br>
	 * This method will return template from cache if the latest file is loaded.
	 * 
	 * @param xslFile
	 * @return
	 * @throws TransformerException
	 */
	private synchronized static final Templates loadXSL(File xslFile)
			throws TransformerException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("XMLUtils.loadXSL() started");
		}
		Templates templates = null;
		Source xslSource = null;
		TransformerFactory transformerFactory = null;
		try {
			if (CACHED_TEMPLATES_CHECK.containsKey(xslFile.getAbsolutePath())
					&& (CACHED_TEMPLATES_CHECK.get(xslFile.getAbsolutePath()) >= xslFile
							.lastModified())) {
				templates = CACHED_TEMPLATES.get(xslFile.getAbsolutePath());
			} else {
				xslSource = new StreamSource(xslFile);
				transformerFactory = TransformerFactory.newInstance();
				templates = transformerFactory.newTemplates(xslSource);
				CACHED_TEMPLATES.put(xslFile.getAbsolutePath(), templates);
				CACHED_TEMPLATES_CHECK.put(xslFile.getAbsolutePath(),
						xslFile.lastModified());
			}
			return templates;
		} finally {
			xslSource = null;
			transformerFactory = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("XMLUtils.loadXSL() completed");
			}
		}
	}

	public static final String doXSLTransform(final String xslFileName,
			final String xml) throws TransformerException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("XMLUtils.doXSLTransform() started");
		}
		try {
			return doXSLTransform(xslFileName, xml, null);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("XMLUtils.doXSLTransform() completed");
			}
		}
	}

	/**
	 * Performs XSL transformation based on the XSL template file name specified
	 * for the input XML.<br>
	 * The XSL templates are cached and auto updated once it is modified.
	 * 
	 * @param xslFileName
	 * @param xml
	 * @param parameters
	 * @return
	 * @throws TransformerException
	 */
	public static final String doXSLTransform(final String xslFileName,
			final String xml, final LinkedHashMap<String, Object> parameters)
			throws TransformerException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("XMLUtils.doXSLTransform() started");
		}
		Source xmlInputSource = null;
		StringReader stringReader = null;
		StringWriter stringWriter = null;
		StreamResult xmlOutputResult = null;
		File xslFile = null;
		Templates templates = null;
		Transformer transformer = null;
		Iterator<String> paramsIterator = null;
		String parameterName = null;
		try {
			xslFile = new File(xslFileName);
			if (CACHED_TEMPLATES_CHECK.containsKey(xslFile.getAbsolutePath())
					&& (CACHED_TEMPLATES_CHECK.get(xslFile.getAbsolutePath()) >= xslFile
							.lastModified())) {
				templates = CACHED_TEMPLATES.get(xslFile.getAbsolutePath());

			} else {
				templates = loadXSL(xslFile);
			}
			stringReader = new StringReader(xml);
			stringWriter = new StringWriter();
			xmlInputSource = new StreamSource(stringReader);
			xmlOutputResult = new StreamResult(stringWriter);
			transformer = templates.newTransformer();
			if (parameters != null && !parameters.isEmpty()) {
				paramsIterator = parameters.keySet().iterator();
				while (paramsIterator.hasNext()) {
					parameterName = paramsIterator.next();
					transformer.setParameter(parameterName,
							parameters.get(parameterName));
				}
			}
			transformer.transform(xmlInputSource, xmlOutputResult);
			return xmlOutputResult.getWriter().toString();
		} finally {
			xmlInputSource = null;
			stringReader = null;
			stringWriter = null;
			xmlOutputResult = null;
			xslFile = null;
			templates = null;
			transformer = null;
			paramsIterator = null;
			parameterName = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("XMLUtils.doXSLTransform() completed");
			}
		}
	}
	
	
	public static void main(String[] args) {
		System.out.println(javax.naming.Context.SECURITY_PRINCIPAL);
		System.out.println(javax.naming.Context.SECURITY_CREDENTIALS);
	}

}
