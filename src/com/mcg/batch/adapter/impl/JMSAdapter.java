/**
 * 
 */
package com.mcg.batch.adapter.impl;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.NonRetryableExecption;
import com.mcg.batch.exceptions.RetryableException;
import com.mcg.batch.utils.MapUtils;
import com.mcg.batch.utils.StringHolder;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * JMS Adapter for sending and receiving the JMS messages. This adapter makes
 * use of the Spring {@link JmsTemplate} to send and received the message.<br>
 * The {@link JmsTemplate} is the resource for this adapter and is expected to
 * be autowired at runtime through the bean xml configuration.
 * 
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */

public final class JMSAdapter extends BaseBatchAdapter<JmsTemplate> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JMSAdapter.class);
	public static final int SEND_MESSAGE = 0;
	public static final int SEND_MESSAGES = 1;
	public static final int SEND_MESSAGE_WITH_CORR_ID = 2;
	public static final int SEND_MESSAGE_WITH_PROP = 3;
	public static final int SEND_MESSAGE_WITH_CORR_ID_PROP = 4;
	public static final int REC_AND_CONVERT_SELECTED = 5;
	public static final int REC_AND_CONVERT = 6;
	public static final int REC_DS_AND_CONVERT = 7;
	public static final int REC_DS_AND_CONVERT_SELECTED = 8;

	public static final String SEND_MESSAGE_OPR = "send";
	public static final String SEND_MESSAGES_OPR = "sendMessages";
	public static final String SEND_MESSAGE_WITH_CORR_ID_OPR = "sendMessage";
	public static final String SEND_MESSAGE_WITH_PROP_OPR = "sendWithProperties";
	public static final String SEND_MESSAGE_WITH_CORR_ID_PROP_OPR = "sendWithPropetiesAndCorrId";
	public static final String REC_AND_CONVERT_SELECTED_OPR = "receiveSelectedAndConvert";
	public static final String REC_AND_CONVERT_OPR = "receiveAndConvert";
	public static final String REC_DS_AND_CONVERT_OPR = "receiveDSAndConvert";
	public static final String REC_DS_AND_CONVERT_SELECTED_OPR = "receiveDSSelectedAndConvert";
	int maxMessageCount ;
	int readMessageCount;

	static {

		Method[] methods = JMSAdapter.class.getDeclaredMethods();

		for (int i = 0; i < methods.length; i++) {
			if (SEND_MESSAGE_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, SEND_MESSAGE, methods[i]);
			} else if (SEND_MESSAGES_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, SEND_MESSAGES, methods[i]);
			}

			else if (SEND_MESSAGE_WITH_CORR_ID_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, SEND_MESSAGE_WITH_CORR_ID,
						methods[i]);
			} else if (SEND_MESSAGE_WITH_PROP_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, SEND_MESSAGE_WITH_PROP, methods[i]);
			} else if (SEND_MESSAGE_WITH_CORR_ID_PROP_OPR.equals(methods[i]
					.getName())) {
				addMethod(JMSAdapter.class, SEND_MESSAGE_WITH_CORR_ID_PROP,
						methods[i]);
			} else if (REC_AND_CONVERT_SELECTED_OPR
					.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, REC_AND_CONVERT_SELECTED,
						methods[i]);
			}

			else if (REC_AND_CONVERT_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, REC_AND_CONVERT, methods[i]);
			} else if (REC_DS_AND_CONVERT_OPR.equals(methods[i].getName())) {
				addMethod(JMSAdapter.class, REC_DS_AND_CONVERT, methods[i]);
			} else if (REC_DS_AND_CONVERT_SELECTED_OPR.equals(methods[i]
					.getName())) {
				addMethod(JMSAdapter.class, REC_DS_AND_CONVERT_SELECTED,
						methods[i]);
			}
		}

	}


	/**
	 * Sends a message using the autowired {@link JmsTemplate}. The parameter is
	 * checked if it is {@link String} or {@link Byte}[] to use
	 * {@link TextMessage} or {@link BytesMessage} respectively. All other types
	 * are sent as {@link ObjectMessage}
	 * 
	 * @param message
	 */

	public void send(final String destinationName, final Serializable message)
			throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending message " + message);
		}
		sendMessage(destinationName, message, null);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Message sent Successfully");
		}
	}

	/**
	 * Sends a list of messages using the autowired {@link JmsTemplate}. The
	 * parameter is checked if it is {@link String} or {@link Byte}[] to use
	 * {@link TextMessage} or {@link BytesMessage} respectively. All other types
	 * are sent as {@link ObjectMessage}
	 * 
	 * @param destinationName
	 * @param message
	 */
	public void sendMessages(final String destinationName,
			final List<? extends Serializable> messages)
			throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.send() started");
		}
		try {
			int sentCount = resource.execute(getdestination(destinationName),
					new ProducerCallback<Integer>() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * org.springframework.jms.core.ProducerCallback#doInJms
						 * (javax.jms.Session, javax.jms.MessageProducer)
						 */
						@Override
						public Integer doInJms(Session session,
								MessageProducer producer) throws JMSException {
							Message jmsMsg = null;
							int sentCount = 0;
							for (Serializable message : messages) {
								try {
									if (message instanceof String) {
										jmsMsg = session.createTextMessage();
										((TextMessage) jmsMsg)
												.setText((String) message);
									} else if (message instanceof byte[]) {
										jmsMsg = session.createBytesMessage();
										((BytesMessage) jmsMsg)
												.writeBytes((byte[]) message);
									} else if (message instanceof StringHolder) {
										jmsMsg = session.createTextMessage();
										((TextMessage) jmsMsg)
										.setText(((StringHolder) message).getValue());
									}
									else {
										jmsMsg = session
												.createObjectMessage(message);
									}

									producer.send(jmsMsg);
									sentCount += 1;
								} catch (Exception ex) {
									JMSException jmsException = new JMSException(
											ex.getMessage());
									jmsException.setLinkedException(ex);
									throw jmsException;
								} finally {
									jmsMsg = null;
								}
							}

							return sentCount;
						}
					});
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(sentCount + " messages were succesfully sent");
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.send() completed");
			}
		}

	}

	/**
	 * Sends a message using the autowired {@link JmsTemplate}. The parameter is
	 * checked if it is {@link String} or {@link Byte}[] to use
	 * {@link TextMessage} or {@link BytesMessage} respectively. All other types
	 * are sent as {@link ObjectMessage} If the correlationId
	 *
	 * @param message
	 * @param correlationId
	 */

	public void sendMessage(final String destinationName,
			final Serializable message, final String correlationId)
			throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending message " + message + " with correlationid "
					+ correlationId);
		}
		try {

			resource.send(getdestination(destinationName),
					new MessageCreator() {
						public Message createMessage(final Session session)
								throws JMSException {
							Message jmsMsg = null;
							try {
								if (message instanceof String) {
									jmsMsg = session.createTextMessage();
									((TextMessage) jmsMsg)
											.setText((String) message);
								} else if (message instanceof byte[]) {
									jmsMsg = session.createBytesMessage();
									((BytesMessage) jmsMsg)
											.writeBytes((byte[]) message);
								}else if (message instanceof StringHolder) {
									jmsMsg = session.createTextMessage();
									((TextMessage) jmsMsg)
									.setText(((StringHolder) message).getValue());
								}else {
									jmsMsg = session.createObjectMessage();
									((ObjectMessage) jmsMsg).setObject(message);
									if (LOGGER.isDebugEnabled()) {
										LOGGER.debug("The object set is "
												+ ((ObjectMessage) jmsMsg)
														.getObject());
									}
								}
								if (jmsMsg != null && correlationId != null) {
									jmsMsg.setJMSCorrelationID(correlationId);
								}
								return jmsMsg;
							} catch (Exception ex) {
								JMSException jmsException = new JMSException(ex
										.getMessage());
								jmsException.setLinkedException(ex);
								throw jmsException;
							}
						}
					});
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Message with correlation id sent successfully");
			}
		}

	}

	/**
	 * Send the messages without correlationId
	 * 
	 * @param destinationName
	 * @param message
	 * @param properties
	 */
	public void sendWithProperties(final String destinationName,
			final Serializable message, final HashMap<String, Object> properties)
			throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending message " + message + " with properties "
					+ properties);
		}
		sendWithPropetiesAndCorrId(destinationName, message, null, properties);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Message with properties sent successfully");
		}
	}

	/**
	 * Send message with properties and correlation id set
	 * 
	 * @param destinationName
	 * @param message
	 * @param correlationId
	 * @param properties
	 */
	public void sendWithPropetiesAndCorrId(final String destinationName,
			final Serializable message, final String correlationId,
			final HashMap<String, Object> properties) throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending Message " + message + " with correlation id "
					+ correlationId + " and properties " + properties);
		}

		resource.send(getdestination(destinationName), new MessageCreator() {

			public Message createMessage(final Session session)
					throws JMSException {
				Message jmsMsg = null;
				Iterator<String> ite = null;
				String key = null;
				Object value = null;
				try {
					if (message instanceof String) {
						jmsMsg = session.createTextMessage();
						((TextMessage) jmsMsg).setText((String) message);
					} else if (message instanceof byte[]) {
						jmsMsg = session.createBytesMessage();
						((BytesMessage) jmsMsg).writeBytes((byte[]) message);
					}else if (message instanceof StringHolder) {
						jmsMsg = session.createTextMessage();
						((TextMessage) jmsMsg)
						.setText(((StringHolder) message).getValue());
					}else {
						jmsMsg = session.createObjectMessage(message);
					}

					if (jmsMsg != null) {
						if (correlationId != null) {
							jmsMsg.setJMSCorrelationID(correlationId);
						}
						
						if (MapUtils.isNotEmpty(properties)) {
							for (Map.Entry<String, Object> entry : properties.entrySet()) {
							    String mapkey = entry.getKey();
							    Object mapvalue = entry.getValue();
							   
								if (mapvalue != null) {
									if (mapvalue instanceof String) {
										jmsMsg.setStringProperty(mapkey,
												(String) mapvalue);
									} else if (mapvalue instanceof Long) {
										jmsMsg.setLongProperty(mapkey,
												(Long) mapvalue);
									} else if (mapvalue instanceof Integer) {
										jmsMsg.setIntProperty(mapkey,
												(Integer) mapvalue);
									} else if (mapvalue instanceof Float) {
										jmsMsg.setFloatProperty(mapkey,
												(Float) mapvalue);
									} else if (mapvalue instanceof Double) {
										jmsMsg.setDoubleProperty(mapkey,
												(Double) mapvalue);
									} else if (mapvalue instanceof Boolean) {
										jmsMsg.setBooleanProperty(mapkey,
												(Boolean) mapvalue);
									} else if (mapvalue instanceof Byte) {
										jmsMsg.setByteProperty(mapkey,
												(Byte) mapvalue);
									} else {
										jmsMsg.setObjectProperty(mapkey, mapvalue);
									}
								}
							}
							
							/*for (ite = properties.keySet().iterator(); ite
									.hasNext(); key = ite.next()) {
								value = properties.get(key);
								
							}*/
						}
					}
					return jmsMsg;

				} catch (Exception ex) {
				    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
					JMSException jmsException = new JMSException(ex
							.getMessage());
					jmsException.setLinkedException(ex);
					throw jmsException;
				} finally {
					jmsMsg = null;
				}
			}
		});

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("message sent with correlation id and properties");
		}
	}

	/**
	 * 
	 * Received the message and converts it to the object using the convert in
	 * JMS Template method.
	 * 
	 * @return
	 */

	public Object receiveAndConvert(final String destinationName,
			final long timeout) throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.receiveAndConvert() started");
		}
		try {
			return receiveSelectedAndConvert(destinationName, timeout, null);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.receiveAndConvert() completed");
			}
		}

	}

	public Object receiveDSAndConvert(final String destinationName,
			final String durableSubscriberName, final Long timeout)
			throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.receiveDSAndConvert() started");
		}
		try {
			return receiveDSSelectedAndConvert(destinationName,
					durableSubscriberName, timeout, null);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.receiveDSAndConvert() completed");
			}
		}

	}

	/**
	 * Receive message from a topic as a durable subscriber.
	 * 
	 * 
	 * @param destinationName
	 * @param durableSubscriberName
	 * @param timeout
	 * @param messageSelector
	 * @return
	 * @throws AdapterException
	 */
	public Object receiveDSSelectedAndConvert(final String destinationName,
			final String durableSubscriberName, final Long timeout,
			final String messageSelector) throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.receiveDSAndConvert() started");
		}
		Message message = null;
		Object object = null;
		
       if(ThreadContextUtils.getExecutionContext().containsKey("maxMessageCount")){
		 maxMessageCount = ThreadContextUtils.getExecutionContext().getInt("maxMessageCount");
       }
       if(ThreadContextUtils.getExecutionContext().containsKey("readMessageCount")){
		 readMessageCount = ThreadContextUtils.getExecutionContext().getInt("readMessageCount");
       }
		try {
			if(maxMessageCount <= 0 || readMessageCount < maxMessageCount){
			message = resource.execute(new SessionCallback<Message>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.springframework.jms.core.SessionCallback#doInJms(javax
				 * .jms.Session)
				 */
				@Override
				public Message doInJms(Session session) throws JMSException {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() started");
					}
					
					TopicSubscriber consumer = null;
					Message message = null;
					try {
						consumer = session.createDurableSubscriber(
								((Topic) getdestination(destinationName)),
								durableSubscriberName, messageSelector, false);
						if (timeout <= 0) {
							message = consumer.receiveNoWait();

						} else {
							message = consumer.receive(timeout);
						}
						if(message != null){
							if(Session.CLIENT_ACKNOWLEDGE == resource.getSessionAcknowledgeMode()){
								 message.acknowledge();
							}
						}	
						return message;
					} catch (Exception ex) {
						JMSException jmsexcep = new JMSException(ex
								.getMessage());
						
						jmsexcep.setLinkedException(ex);
						throw jmsexcep;

					} finally {
						JmsUtils.closeMessageConsumer(consumer);

						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() completed");
						}
					}
				}
			}, true);
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("JMS Acknowledge Mode : "+ resource.getSessionAcknowledgeMode());
			}
			return  resource.getMessageConverter().fromMessage(message);
		} catch (MessageConversionException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception in converting the message", e);
			}
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			throw new NonRetryableExecption(e);
		} catch (JMSException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			if (e.getLinkedException() instanceof NonRetryableExecption) {
				throw (NonRetryableExecption) e.getLinkedException();
			} else {
				throw new RetryableException(e);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.receiveDSAndConvert() completed");
			}
		}

	}

	/**
	 * Received the message with the specified message selector and converts it
	 * to the object using the convert in JMS Template method
	 * 
	 * @param messageSelector
	 *            String
	 * @return Object
	 */
	public Object receiveSelectedAndConvert(final String destinationName,
			final Long timeout, final String messageSelector)
			throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.receiveAndConvert() started");
		}
		Message message = null;
			try {
	
				message = resource.execute(new SessionCallback<Message>() {
				/* (non-Javadoc)
				 * 
				 * @see
				 * org.springframework.jms.core.SessionCallback#doInJms(javax
				 * .jms.Session)*/
				 
				@Override
				public Message doInJms(Session session) throws JMSException {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() started");
					}
					MessageConsumer consumer = null;
					Message message = null;
					try {
						consumer = session.createConsumer(
								getdestination(destinationName),
								messageSelector);
						if (timeout <= 0) {
							message = consumer.receiveNoWait();

						} else {
							message = consumer.receive(timeout);					
						}	
						return message;
					} catch (Exception ex) {
						JMSException jmsException = new JMSException(ex
								.getMessage());
						jmsException.setLinkedException(ex);
						throw jmsException;
					} finally {
						JmsUtils.closeMessageConsumer(consumer);
						
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() completed");
						}
					}
				}
			}, true);
			
			return  resource.getMessageConverter().fromMessage(message);
		} catch (MessageConversionException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception in converting the message", e);
			}
			throw new NonRetryableExecption(e);
		} catch (JMSException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			if (e.getLinkedException() instanceof NonRetryableExecption) {
				throw (NonRetryableExecption) e.getLinkedException();
			} else {
				throw new RetryableException(e);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.receiveAndConvert() completed");
			}
		}
	}

	/**
	 * Received the message with the specified message selector and converts it
	 * to the object using the convert in JMS Template method
	 * 
	 * @param messageSelector
	 *            String
	 * @return Object
	 */
	/*public Object receiveSelectedAndConvert(final String destinationName,
			final Long timeout, final String messageSelector)
			throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.receiveAndConvert() started");
		}
		Message message = null;
		Object object = null;
	       if(ThreadContextUtils.getExecutionContext().containsKey("maxMessageCount")){
			 maxMessageCount = ThreadContextUtils.getExecutionContext().getInt("maxMessageCount");
	       }
	       if(ThreadContextUtils.getExecutionContext().containsKey("readMessageCount")){
			 readMessageCount = ThreadContextUtils.getExecutionContext().getInt("readMessageCount");
	       }
			try {
				
				if(readMessageCount < maxMessageCount){
				message = resource.execute(new SessionCallback<Message>() {
				
				 (non-Javadoc)
				 * 
				 * @see
				 * org.springframework.jms.core.SessionCallback#doInJms(javax
				 * .jms.Session)
				 
				@Override
				public Message doInJms(Session session) throws JMSException {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() started");
					}
					MessageConsumer consumer = null;
					Message message = null;
					try {
						consumer = session.createConsumer(
								getdestination(destinationName),
								messageSelector);
						if (timeout <= 0) {
							message = consumer.receiveNoWait();

						} else {
							message = consumer.receive(timeout);
							
						}
						
						
						if(message != null){
							  if(Session.CLIENT_ACKNOWLEDGE == resource.getSessionAcknowledgeMode()){
								  LOGGER.info("Entered acknowledge condition");
								   message.acknowledge();
								}
						}
						
					
						return message;
					} catch (Exception ex) {
						JMSException jmsException = new JMSException(ex
								.getMessage());
						jmsException.setLinkedException(ex);
						throw jmsException;
					} finally {
						JmsUtils.closeMessageConsumer(consumer);
						
						
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("JMSAdapter.receiveAndConvert(...).new SessionCallback() {...}.doInJms() completed");
						}
					}
				}
			}, true);
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("JMS Acknowledge Mode : "+resource.getSessionAcknowledgeMode()+" readMessageCount : "+readMessageCount+" maxMessageCount : "+maxMessageCount);
			}
			// Setting acknowledge for client acknowledge mode
			if(message != null){
			  
			 	object = getMessageConverter(message);
			}
			
			return  resource.getMessageConverter().fromMessage(message);
		} catch (MessageConversionException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception in converting the message", e);
			}
			throw new NonRetryableExecption(e);
		} catch (JMSException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			if (e.getLinkedException() instanceof NonRetryableExecption) {
				throw (NonRetryableExecption) e.getLinkedException();
			} else {
				throw new RetryableException(e);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.receiveAndConvert() completed");
			}
		}
	}*/
	public Object getMessageConverter(Message message){
		Object object = null;
		try {
			 object =  resource.getMessageConverter().fromMessage(message);
		} catch (MessageConversionException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception in converting the message", e);
			}
		} catch (JMSException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception in converting the message", e);
			}
		}
		return object;
	}
	
	private Destination getdestination(String destinationName)
			throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSAdapter.getdestination() started");
		}
		Destination destination = null;
		try {
			destination = (Destination) ((JndiDestinationResolver) resource
					.getDestinationResolver()).getJndiTemplate().lookup(
					destinationName);
		} catch (NamingException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, destinationName);
			LOGGER.error("Unable to lookup destination by name"
					+ destinationName, e);
			throw new NonRetryableExecption(
					"unable to lookup Destination By Name " + destinationName,
					e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSAdapter.getdestination() completed");
			}

		}
		return destination;
	}

}