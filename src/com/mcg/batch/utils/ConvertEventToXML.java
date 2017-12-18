
package com.mcg.batch.utils;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mcg.batch.events.Event;
import com.mcg.batch.events.EventElement;

/**
 * @author BHMO
 *
 */
public class ConvertEventToXML {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConvertEventToXML.class);
	/**
	 * 
	 */
	public ConvertEventToXML() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	

	public static String convertEventToXML(Object objEvent) {
		if(LOGGER.isInfoEnabled())
		{
            LOGGER.info("Entered " + objEvent.toString());
		}
		String xmlString = null;
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        try {
			icBuilder = icFactory.newDocumentBuilder();
            Document doc = icBuilder.newDocument();
            Element mainRootElement = doc.createElement("event");
            doc.appendChild(mainRootElement);
            @SuppressWarnings("unchecked")
			Event<EventElement, EventElement>  event =  (Event<EventElement, EventElement>)objEvent;
            mainRootElement.appendChild(createChild(doc,event,"header"));
            mainRootElement.appendChild(createChild(doc,event,"body" ));
           
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
			
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult console = new StreamResult(bytestream);
			transformer.transform(source, console);
			xmlString = new String(bytestream.toByteArray());
	        } 
             catch (ParserConfigurationException e) {
			    e.printStackTrace();
		    }catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			}catch (TransformerException e) {
				e.printStackTrace();
			}
      		return xmlString;
    }
 
   private static Node createChild(Document doc, Event<EventElement, EventElement> event,String tag) {
	   
        Element element = doc.createElement(tag);
        List<EventElement> elementList = null;
        if(tag != null && tag != "" && tag.equals("body")){
        	elementList = event.getBody();
        }
        else if(tag != null && tag != "" && tag.equals("header")){
        	elementList = event.getHeader();
        }
        String key = "", value = "";
        for (int i = 0; i < elementList.size(); i++) {
        	
        	if(elementList.get(i).getKey()!=null && elementList.get(i).getKey()!="" ){
        		key = elementList.get(i).getKey().trim();
        	}
            if(!(elementList.get(i).toString().contains("null")) && elementList.get(i) != null && elementList.get(i).getValue()!=null &&elementList.get(i).getValue()!="" ){	
        		value = elementList.get(i).getValue().toString().trim();
        	}
            element.appendChild(createTextNode(doc, element, key, value));
		} 
    return element;
    }
   
    // utility method to create text node
    private static Node createTextNode(Document doc, Element element, String name, String value) {
    	Element node = null;
        node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
 

}
