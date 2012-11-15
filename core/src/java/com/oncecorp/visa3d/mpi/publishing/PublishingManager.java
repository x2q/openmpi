/**
 * Copyright 2003, 2004  ONCE Corporation
 *
 * LICENSE:
 * This file is part of BuilditMPI. It may be redistributed and/or modified
 * under the terms of the Common Public License, version 1.0.
 * You should have received a copy of the Common Public License along with this
 * software. See LICENSE.txt for details. Otherwise, you may find it online at:
 *   http://www.oncecorp.com/CPL10/ or http://opensource.org/licenses/cpl.php
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES NO REPRESENTATIONS OR
 * WARRANTIES, EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW,
 * IN NO EVENT WILL THE AUTHOR BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT
 * LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL, INDIRECT,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY FURNISHING,
 * PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR HAVE
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * -----------------------------------------------------
 * $Id$
 */

package com.oncecorp.visa3d.mpi.publishing;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.controller.ExecutionControl;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.security.TripleDESEncrypter;
import com.oncecorp.visa3d.mpi.utility.Utils;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

/**
 * The Publishing manager class is used to control and interact with the properly configured
 * JSM topic. This interaction is mainly a publishing action of all the processed messages
 * within the Core Server. If, for any reasons, the Core Server is unable to publish messages,
 * then a potential data loss is possible. In that case, the Core Server is notified by the
 * PublishingManager to stop it's activies and wait until the JMS connection is back online.
 *
 * @version $Revision: 27 $
 * @author	Alan Zhang
 *
 * Patented optimizations strategies by
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class PublishingManager {

	final static String MSG_PROP_MESSAGE_TYPE = "MessageType";
	final static String MSG_PROP_MERCHANT_ID = "MerchantID";
	final static String MSG_PROP_MESSAGE_VERSION = "MessageVersion";
	final static String MSG_PROP_ENCRYPTION_MODE = "EncryptionMode";
	final static String MSG_PROP_IV = "EncryptionIV";
	final static String MSG_PROP_PROTOCOL = "Protocol";

	final static String JMS_CONFIG_PREFIX = "JMS.";

	final static String ERROR_NOTIFICATION_MSG =
		"Error in JMS Publishing Service occurred. ONCEmpi Core Server is stopped by JMS Publishing Manager.";

	/**
	 * Local log4j logger
	 */
	static protected Logger logger =
		MPILogger.getLogger(PublishingManager.class.getName());

	/**
	 * Singleton reference of this class
	 */
	static private PublishingManager manager;

	/**
	 * JMS connection factory JNDI name
	 */
	static private String connFacJndi;

	/**
	 * JMS topic JNDI name
	 */
	static private String topicJndi;

	/**
	 * JMS message encryption mode
	 */
	static private String encryptionMode;

	/**
	 * JMS topic connection factory
	 */
	static private TopicConnectionFactory topicConnectionFactory;
	static private TopicConnection topicConnection = null;
	static private Topic topic;

	/**
	 * Prevent using constructor to create new PublishingManager instance
	 */
	private PublishingManager() {
		try {
			//Retrieve JMS configuration properties
			Config cfg = Config.getConfigReference();

			setConnFacJndi(
				(String) cfg.getConfigData(
					MPIConfigDefinition.JMS_CONNECTION_FACTORY_JNDI));
			setTopicJndi(
				(String) cfg.getConfigData(MPIConfigDefinition.JMS_TOPIC_JNDI));
			setEncryptionMode(
				(String) cfg.getConfigData(
					MPIConfigDefinition.JMS_ENCRYPTION_MODE));

			logger.debug(
				"JMS configuration properties retrieved. "
					+ "Factory JNDI: "
					+ getConnFacJndi()
					+ ", Topic JNDI: "
					+ getTopicJndi()
					+ ", Encryption Mode: "
					+ getEncryptionMode());

			// Lookup connection factory and topic
			InitialContext initialContext = null;
			String defaultMode =
				(String) cfg.getConfigData(
					MPIConfigDefinition.JMS_NAMING_SERVICE_DEFAULT_MODE);
			if (defaultMode.equalsIgnoreCase("true")) {
				// default mode
				initialContext = new InitialContext();
			} else {
				// Prepare environment for IntitalContext
				Hashtable env = new Hashtable();
				setupContextEnvironment(cfg, env);

				initialContext = new InitialContext(env);
			}

			Object obj = initialContext.lookup(getConnFacJndi());
			TopicConnectionFactory factory =
				(TopicConnectionFactory) PortableRemoteObject.narrow(
					obj,
					TopicConnectionFactory.class);
			setTopicConnectionFactory(factory);

			/*
			 * [Martin's Note: Feb 11, 2003 12:29:04 PM]
			 * Optimizing the JMSPublisher to cache the JMS connection in as an instance
			 * member. According to the JMS Spec, the JMS topic connection is a heavyweight
			 * object and the creation of it incur a lot of CPU cycles.
			 *
			 * By caching the connection, the publishing activity will be optimized and speed up
			 *
			 */
			checkTopicConnection();

			obj = initialContext.lookup(getTopicJndi());
			setTopic((Topic) PortableRemoteObject.narrow(obj, Topic.class));
			logger.info("JMS init: Topic found.");
		} catch (PublishException publishExcep) {
			logger.error(
				"Unable to create connection to JMS provider",
				publishExcep);
			notifyErrorToAuthenticator();
		} catch (ConfigurationException ce) {
			logger.error(
				"Failed to retrieve JMS configuration properties.",
				ce);
			notifyErrorToAuthenticator();
			return;
		} catch (NamingException ne) {
			logger.error("JMS init failed.", ne);
			notifyErrorToAuthenticator();
		}
	}

	private void setupContextEnvironment(Config cfg, Hashtable env)
		throws ConfigurationException {
		env.put(
			Context.INITIAL_CONTEXT_FACTORY,
			cfg.getConfigData(MPIConfigDefinition.JMS_INITIAL_CONTEXT_FACTORY));
		env.put(
			Context.PROVIDER_URL,
			cfg.getConfigData(
				MPIConfigDefinition.JMS_NAMING_SERVICE_PROVIDER_URL));
		env.put(
			Context.SECURITY_PRINCIPAL,
			cfg.getConfigData(
				MPIConfigDefinition.JMS_NAMING_SERVICE_SECURITY_PRINCIPAL));
		env.put(
			Context.SECURITY_CREDENTIALS,
			cfg.getConfigData(
				MPIConfigDefinition.JMS_NAMING_SERVICE_SECURITY_CREDENTIALS));
		env.putAll(cfg.getConfigDataWithPrefix(JMS_CONFIG_PREFIX));
	}

	/**
	 * Get reference of this class
	 */
	public synchronized static PublishingManager getInstance() {
		if (manager == null) {
			setManager(new PublishingManager());
			logger.info("JMS Publishing Manager init finished.");
		}

		return getManager();
	}

	/**
	 * Reset publishManager instance by retrieving latest configuration.
	 * This will be called by JMX MBean whenever core configuration data
	 * changed.
	 */
	public synchronized static void reset() {
		// checks Authenticator servlet status.
		if (ExecutionControl
			.getInstance()
			.getExecutionStatus()
			.equals(ExecutionControl.STATUS_STOPPED_BY_CORE)) {
			logger.debug("Reset Authenticator Servlet to 'START' status.");
			ExecutionControl.getInstance().start();
		}

		// resets cached JMS reference
		setManager(null);
		setTopicConnectionFactory(null);
		setTopic(null);
		setTopicConnection(null);

		// initializes PublishingManager
		setManager(new PublishingManager());
		
		// Only reset the 3DES encrypter if we need encryption of JMS messages
		if (getEncryptionMode().equalsIgnoreCase("true")) {
			TripleDESEncrypter.reset();
			logger.debug("TripleDESEncrypter properly resetted");
		}
		logger.debug("PublishingManager properly resetted.");

	}

	/**
	 * Notify Authenticator Servlet to stop processing upcoming requests
	 */
	private void notifyErrorToAuthenticator() {
		ExecutionControl.getInstance().stop(
			ExecutionControl.STATUS_STOPPED_BY_CORE,
			ERROR_NOTIFICATION_MSG);
		logger.warn(
			"Authenticator Server has been stopped by PublishingManager.");
	}

	/**
	 * Publish message to JMS provider
	 *
	 * @param msg The message to be published
	 * @param merchantId The merchant ID for current transcation
	 */
	public void publish(Message msg)
		throws PublishException {
		try {
			logger.info("Publishing message: " + msg.getId());

			/*
			 * [Martin's Note: Feb 11, 2003 12:48:44 PM]
			 *
			 * According to the JMS spec (page 26), session & publisher does not
			 * support concurrent usage. So we cannot cache then in the PublishingManager
			 * singleton instance.
			 */

			// We need to ensure that we have a live topicConnection. If not then we need to
			// reestablish that connection
			checkTopicConnection();

			// Create session
			TopicSession session =
				topicConnection.createTopicSession(
					false,
					Session.AUTO_ACKNOWLEDGE);

			// Create publisher
			TopicPublisher publisher = session.createPublisher(getTopic());

			// Prepare text message
			TextMessage textMsg = session.createTextMessage();

			// Setting Message type header field
			textMsg.setStringProperty(MSG_PROP_MESSAGE_TYPE, msg.getType());

			// Setting Message version header field
			textMsg.setStringProperty(MSG_PROP_MESSAGE_VERSION, msg.getVersion());

			// Setting Merchant ID header field
			textMsg.setStringProperty(MSG_PROP_MERCHANT_ID, AuthenticatorSession.instance().getMerchantID());

			// Setting Payment protocol header field
			textMsg.setStringProperty(MSG_PROP_PROTOCOL,
                    "" + Utils.toProtocolByte(
                              AuthenticatorSession.instance().getProtocol() ) );

			// Setting Encryption mode header field
			textMsg.setStringProperty(MSG_PROP_ENCRYPTION_MODE, getEncryptionMode());

			String msgBody = msg.toString();
			if (getEncryptionMode().equalsIgnoreCase("true")) {
				//Do encryption here
				TripleDESEncrypter encrypter = TripleDESEncrypter.getInstance();

				String[] result;
				try {
					result = encrypter.encrypt(msgBody);
				} catch (Exception e) {
					logger.error("Encryption error occurred.", e);
					notifyErrorToAuthenticator();

					throw new PublishException(e.getMessage());
				}

				if ((result == null) || (result.length != 2)) {
					logger.error(
						"Encryption result is null or with incorrect length.");
					throw new PublishException("Encryption result is null or with incorrect length.");
				}

				textMsg.setText(result[0]);
				textMsg.setStringProperty(MSG_PROP_IV, result[1]);
			} else {
				textMsg.setText(msgBody);
			}

			//publishing
			publisher.publish(textMsg);
			logger.info("Publishing finished.");

		} catch (JMSException jmse) {
			logger.error("Failed to publish message: " + msg.getId(), jmse);
			notifyErrorToAuthenticator();

			throw new PublishException(jmse.getMessage());
		}
	}

	/**
	 * Check to ensure that the TopicConnection is up and alive.
	 */
	private void checkTopicConnection() throws PublishException {
		if (topicConnection == null) {
			try {
				// Initialize JMS connection
				topicConnection =
					getTopicConnectionFactory().createTopicConnection();
				logger.info("JMS init: Topic Connection created.");
			} catch (JMSException e) {
				throw new PublishException();
			}
		} else {
			// Connection is created ensure it is valid by starting it
			try {
				topicConnection.start();
			} catch (JMSException e) {
				// Unable to start then we recreate the connection
				topicConnection = null;
				checkTopicConnection();
			}
		}

	}

	/**
	 * Returns the connFacJndi.
	 * @return String
	 */
	public static String getConnFacJndi() {
		return connFacJndi;
	}

	/**
	 * Returns the encryptionMode.
	 * @return String
	 */
	public static String getEncryptionMode() {
		return encryptionMode;
	}

	/**
	 * Returns the manager.
	 * @return PublishingManager
	 */
	public static PublishingManager getManager() {
		return manager;
	}

	/**
	 * Returns the topicJndi.
	 * @return String
	 */
	public static String getTopicJndi() {
		return topicJndi;
	}

	/**
	 * Sets the connFacJndi.
	 * @param connFacJndi The connFacJndi to set
	 */
	public static void setConnFacJndi(String connFacJndi) {
		PublishingManager.connFacJndi = connFacJndi;
	}

	/**
	 * Sets the encryptionMode.
	 * @param encryptionMode The encryptionMode to set
	 */
	public static void setEncryptionMode(String encryptionMode) {
		PublishingManager.encryptionMode = encryptionMode;
	}

	/**
	 * Sets the manager.
	 * @param manager The manager to set
	 */
	public static void setManager(PublishingManager manager) {
		PublishingManager.manager = manager;
	}

	/**
	 * Sets the topicJndi.
	 * @param topicJndi The topicJndi to set
	 */
	public static void setTopicJndi(String topicJndi) {
		PublishingManager.topicJndi = topicJndi;
	}

	/**
	 * Returns the topic.
	 * @return Topic
	 */
	public static Topic getTopic() {
		return topic;
	}

	/**
	 * Sets the topic.
	 * @param topic The topic to set
	 */
	public static void setTopic(Topic topic) {
		PublishingManager.topic = topic;
	}

	/**
	 * Returns the topicConnectionFactory.
	 * @return TopicConnectionFactory
	 */
	public static TopicConnectionFactory getTopicConnectionFactory() {
		return topicConnectionFactory;
	}

	/**
	 * Sets the topicConnectionFactory.
	 * @param topicConnectionFactory The topicConnectionFactory to set
	 */
	public static void setTopicConnectionFactory(TopicConnectionFactory topicConnectionFactory) {
		PublishingManager.topicConnectionFactory = topicConnectionFactory;
	}

	/**
	 * Ensure that the JMS Connection is properly closed when the PublishingManager
	 * gets garbage collected
	 *
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		// Close the connection if we got oen in the first place
		if (topicConnection != null) {
			try {
				topicConnection.close();
			} catch (JMSException e) {
				logger.error("Failed to close topic connection.", e);
			}
		}

		// Destructor is in reverse order of creation
		super.finalize();

	}
	/**
	 * Returns the topicConnection.
	 * @return TopicConnection
	 */
	public static TopicConnection getTopicConnection() {
		return topicConnection;
	}

	/**
	 * Sets the topicConnection.
	 * @param topicConnection The topicConnection to set
	 */
	public static void setTopicConnection(TopicConnection topicConnection) {
		PublishingManager.topicConnection = topicConnection;
	}

}
