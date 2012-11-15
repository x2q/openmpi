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

package com.oncecorp.visa3d.bridge.listening;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.TopicConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.TopicPublisher;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Session;

import java.util.Properties;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.bridge.listening.ListeningUtils;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.utility.Utils;
import com.oncecorp.visa3d.bridge.security.TripleDESEncrypter;
import com.oncecorp.visa3d.bridge.beans.ListeningMessageBean;
import com.oncecorp.visa3d.bridge.startup.StartupProxy;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: ListeningThread</p>
 * <p>Description: Call JMS methods to create connection, session and subscriber.
 *  Register itself as a message listener to receive the JMS message. Transform
 * the message according to the configue definition( such as field filter, encryption,
 * and mask), then call the registered MPI message listener( auditing logger or plugin
 * channel). Start/stop/Register/Unregister/GetStatus etc functions are also provided for
 * JMX related control.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningThread implements MessageListener
{
    protected static final String TOPIC_CONNECTION_FACTORY =
                        "databridge.tcf";
    protected static final String TOPIC = 			"databridge.topic";

    protected TopicConnection m_connection = null;
    protected TopicSession m_session = null;
    protected String m_listener = null;
    protected String m_clientID = null;
    protected String m_channel = null;
    protected Topic m_topic = null;
    protected TopicSubscriber m_sub = null;

    protected String m_status = Utils.LISTENING_STOP;
    protected boolean m_running = false;

    protected List m_merchantIDs = Collections.synchronizedList( new ArrayList() );
    protected Map m_messages = null;

    protected MPIMessageListener m_plugin = null;

    private static Logger m_logger = DataBridgeLoger.getLogger(
            ListeningThread.class.getName() );


    protected static Hashtable m_jmsProps = null;
    protected static String m_factoryJndi = null;
    protected static String m_topicJndi = null;

    protected boolean m_initFlag = false;

	protected long    startStopTime = System.currentTimeMillis();
	protected String  startStopReason = null;

    /**
     * Init with the listening name, channel name and startup status
     * @param name - listening name
     * @param channel - channel name
     * @param status - initialize status, start or stop
     */
    public void init( String name, String channel, String status )
    {
        m_listener = name;
        m_channel = channel;
        setStatus( status );
		if ( status != null && status.equals(ListeningUtils.PLUGIN_CLASS_NOT_INIT) )
	            setStartStopReason( Utils.PLUGIN_CLASS_NOT_INIT );
        m_clientID = this.generateClientID();
    }

    /**
     * Set JMS related configurable attributes which include JNDI server, Connection
     * Factory JNDI name and topic JNDI name.
     * @param props - hashtable contains JMS related attributes
     * @param factoryJndi - connection factory JNDI name
     * @param topicJndi - connection topic JNDI name
     */
    public static void setJMSContextAttributes( Hashtable props,
            String factoryJndi,  String topicJndi )
    {
        m_logger.debug("Do setJMSContext");
        m_jmsProps = props;
        m_factoryJndi = factoryJndi;
        m_topicJndi = topicJndi;

    }

    /**
     * JMS server maybe run on different environment, this method return the JNDI
     * context which is created from the configurable properties.
     * @return - JMS JNDI context
     */
    public static Context getJMSContext()
    {
        m_logger.debug("Begin getJMSContext");

        if ( m_jmsProps != null )
        {

            try {
                Context ctx = new InitialContext( m_jmsProps );
                m_logger.debug("Success getJMSContext with jms attributes");
                return ctx;
            } catch ( Exception e )
            {
                m_logger.warn("Can't get with JMS properties, use default.", e);
            }

        }

        return StartupProxy.getContext();
    }

    /**
     * @return - connection factory JNDI name
     */
    public static String getConnectionFactoryJNDI()
    {
        if ( m_factoryJndi == null )
            return TOPIC_CONNECTION_FACTORY;
        else
            return m_factoryJndi;
    }

    /**
     *
     * @return - connection topic JNDI name
     */
    public static String getConnectionTopic()
    {
        if ( m_topicJndi == null )
            return TOPIC;
        else
            return m_topicJndi;
    }

    /**
     * Initialize JMS related resources, create connection, session and subscriber.
     * @return - success or not
     */
    public boolean initializeJMS()
    {

        m_logger.debug("Begin initializeJMS with status:" + m_status);

        if ( m_initFlag )
            return true;

        if ( m_plugin == null || !m_plugin.isInitialized() )
        {
            m_logger.debug( "The listener [ " + m_listener
                            + "] hasn't been initialized, don't start listening service for it.");
            setStatus( ListeningUtils.INITIALIZE_ERROR );
			setStartStopReason( Utils.LISTENING_INITIAL_ERROR );
            m_logger.debug("Exit initializeJMS");
            return false;
        }

        try {
              Context context = getJMSContext();

              TopicConnectionFactory tcf = (TopicConnectionFactory)
                          context.lookup( getConnectionFactoryJNDI() );

              if (tcf == null) {
                m_logger.error("Topic connection factory not found!");
				setStartStopReason( Utils.CONNECTION_FACTORY_NOT_FOUND );
                return false;
              }

              // Create a connection
              m_logger.debug("creating connection");
              m_connection = tcf.createTopicConnection();
              m_connection.setExceptionListener( new ConnectionExceptionListener( this ) );
              try {
                  m_connection.setClientID( m_clientID );
              } catch ( Exception cex )
              {
                  m_logger.warn("Client ID alreadt exist", cex);
              }
              m_logger.debug("setting clientid [" + m_clientID + "]");

              // Create a session
              m_session = m_connection.createTopicSession(true,
                      Session.AUTO_ACKNOWLEDGE);

              m_logger.debug("looking up topic");
              m_topic = (Topic) context.lookup( getConnectionTopic() );

              // Get around the problem of publish before subscribe by
              // using a durable subscription.
              m_logger.debug("creating durable subscriber");
              String sstr = getSelectorString();
              m_logger.debug("Selector String is [" + sstr + "]");
//              m_sub = m_session.createDurableSubscriber(m_topic, m_clientID );
              m_sub = m_session.createDurableSubscriber(m_topic, m_clientID,
                     sstr, true );
              m_sub.setMessageListener(this);

              m_initFlag = true;

              if ( !ListeningUtils.isStopAction( m_status ) )
                  this.start();

              m_logger.debug("End initializeJMS");
              return true;

        } catch (Throwable t) {
              m_initFlag = false;
              m_logger.error("In initializeJMS", t);
			  setStartStopReason( Utils.JMS_SETUP_EXCEPTION );
              return false;
        }

    }

    /**
     * Implemente the MessageListener interface, which is automatically called by
     * JMS server when message comes. It decrypt the received message first if needed.
     * Transform it then and delivery it the MPI message listener finnally. If exception
     * is caught or the MPI message listener return false, a ListeningMessageException will
     * be throwed and the message will rollbakc to the queue for the later handler.
     * @param message - JMS message
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message)
    {
        boolean isOk = true;
        String str = "";

        try {
            m_logger.debug( m_clientID + "Get a Message.");
            TextMessage text = (TextMessage) message;
            String encryptStr = message.getStringProperty( ListeningUtils.JMS_ENCRYPTION_MODE );
            m_logger.debug("Msg is=["
               + message.getStringProperty( ListeningUtils.JMS_MESSAGE_TYPE) + "]["
               + message.getStringProperty( ListeningUtils.JMS_MERCHANT_PROPERTY) + "]["
               + message.getStringProperty( ListeningUtils.JMS_MESSAGE_PROTOCOL) + "]["
               + message.getStringProperty( ListeningUtils.JMS_MESSAGE_VERSION) + "]["
               + encryptStr + "]");
            String body = text.getText();

            // If message is encrypted, decrypt it
            if ( encryptStr != null && encryptStr.trim().equalsIgnoreCase("true") )
            {
                TripleDESEncrypter decrypter = TripleDESEncrypter.getInstance();
                String ivstr = message.getStringProperty( ListeningUtils.JMS_ENCRYPTION_IV );
                body = decrypter.decrypt( body, ivstr );
            }


            String type = message.getStringProperty( ListeningUtils.JMS_MESSAGE_TYPE);
            String version = message.getStringProperty( ListeningUtils.JMS_MESSAGE_VERSION);

            Map messages = null;
            try
            {
                messages = MessageFieldsFilter.getMessageMappingBean().getMessages();
            } catch ( Exception e )
            {
                messages = null;
            }

            if ( messages != null && !messages.containsKey( BeansHelper.getMessageMappingKey( type, version ) ) )
            {
                /*
                for ( Iterator lt = messages.keySet().iterator(); lt.hasNext(); )
                {
                    m_logger.debug("message key=" + lt.next() );
                }
                */
                m_logger.debug("Message type [" +  type + "] version ["
                                  + version + "] not defined, ignore it.");
            }
            else
            {

                Properties props = ListeningUtils.getMessageProperties( message );

				Document fromdoc = getXmlDoc( body );
				Element root = MessageFieldsFilter.getMessageTypeRoot(
						fromdoc, type, version );
				if ( root == null )
				{
					m_logger.debug("Message type [" +  type + "] version ["
									  + version + "] not defined correctly, "
		                              + "please check xpath in mpi-messages.xml for the root. "
									  + "The message is ignored.");
				}
				else
				{
					Document doc = transformMsg(type, version, fromdoc, props );

					/**
					 * Call MPI message listener to handler the pre-handled messages.
					 */
					if ( m_plugin != null )
					{
						synchronized ( m_plugin )
						{
							m_logger.debug("Before call plugin handleMsg");

							isOk = m_plugin.handleMsg(
									props, doc );
							if ( !isOk )
                      		     str = " return by listener.";
 	                        m_logger.debug("After call plugin handleMsg");
						}
					}
				}
            }

        } catch (Throwable t) {
            m_logger.error("Exception in onMessage", t);
            str = " as exception happened.";
            isOk = false;
        }

        //Tell JMS to rollback the message
        try {
            if ( !isOk )
            {
//            throw new ListeningMessageException(m_clientID + " onMessageException ["
//                 + str + "]" );
                m_session.rollback();
                m_logger.error(m_clientID + " [" + str + "] is rolled back.");
            }
            else
            {
                m_session.commit();
                m_logger.debug(m_clientID + " message is successfully committed.");
            }
        } catch ( Exception e )
        {
            if ( !isOk )
                m_logger.error("rollback error", e);
            else
                m_logger.error("commit error", e);
        }
    }

    /**
     * Set the current status to exception status
     */
    protected void setExceptionStatus( )
    {
		if ( m_running )
	       startStopTime = System.currentTimeMillis();
        m_status = Utils.LISTENING_EXCEPTION;
		setStartStopReason( Utils.RUNNING_EXCEPTION );
        m_running = false;
    }

	/**
	 * Stop message listening
	 */
	public void stop()
	{
		this.stop( "" );
	}

    /**
     * Stop message listening
     */
    public void stop( String msg )
    {
        m_logger.debug("Enter stop " +  msg);
		setStartStopReason( msg );
        if ( !m_initFlag )
        {
            m_logger.warn("Not initialized, do nothing.");
            return ;
        }

        if ( !m_running && !m_status.equals( Utils.LISTENING_EXCEPTION ))
             return;
        try {
            m_connection.stop();
            if ( m_plugin != null )
            {
                synchronized ( m_plugin )
                {
                     m_plugin.stop();
                }
            }
            m_running = false;
		   startStopTime = System.currentTimeMillis();
        } catch ( Exception e )
        {
            m_status = Utils.LISTENING_EXCEPTION;
            m_logger.error("In stop", e);
        }

        m_status = Utils.LISTENING_STOP;
        m_logger.debug("Exit stop");
    }

    /**
     * Get the current listening thread status
     * @return - the status of current listening thread
     */
    public String getStatus()
    {
        return m_status;
    }

    /**
     *
     * @param aStatus - the new value for status
     */
    public void setStatus(String aStatus){
        m_status = aStatus;
    }


	/**
	 * Start listening thread
	 */
	public void start()
	{
		this.start( "" );
	}

    /**
     * Start listening thread
     */
    public void start( String msg )
    {
        m_logger.debug("Enter start " + msg );
		setStartStopReason( msg );
        if ( m_status.equals( Utils.LISTENING_EXCEPTION ) || !m_initFlag )
        {
            try {
                if ( m_sub != null )
                    m_sub.close();
                m_connection.close();
            } catch ( Exception e )
            {
                m_logger.warn("Try recreate JMS connection.", e);
            }

            m_logger.warn("Exception status or not initialized, do initialize again.");
            m_initFlag = false;
            m_running = false;
            m_status = Utils.LISTENING_STOP; // make initializeJMS not call this again.
            initializeJMS();
            return ;
        }

        if ( m_running )
        {
            m_status = Utils.LISTENTING_START;
             return;
        }
        try {
            if ( m_plugin != null )
            {
                synchronized ( m_plugin )
                {
                    m_plugin.start();
                }
            }
            m_connection.start();
            m_running = true;
			startStopTime = System.currentTimeMillis();
        } catch ( Exception e )
        {
            m_running = false;
            m_status = Utils.LISTENING_EXCEPTION;
			setStartStopReason( Utils.JMS_START_EXCEPTION );
            m_logger.error("In start", e);
        }
        m_status = Utils.LISTENTING_START;
        m_logger.debug("Exit start");

    }

    /**
     * Register this thread
     */
    public void register()
    {
        m_logger.debug("Enter register");

        if ( m_plugin == null )
            return;

        if ( m_connection != null )
        {
            try {
                if ( m_sub != null )
                    m_sub.close();
                m_connection.close();
            } catch ( Exception e )
            {
                m_logger.warn("Try recreate JMS connection.", e);
            }
        }

//        m_status = ListeningUtils.REGISTER;
        m_plugin.register();
        m_initFlag = false;
        this.initializeJMS();
    }

    /**
     * Unregister this thread
     */
    public void unregister()
    {
        m_logger.debug("Enter unregister");
        if ( m_plugin == null || m_connection == null )
            return;

        try {
            m_sub.close();
            m_session.unsubscribe( m_clientID );
            m_connection.stop();
            m_connection.close();
            m_plugin.unregister();

        } catch ( Exception e )
        {
            m_logger.error("Exception during [ListeningThread.unregister]", e);
        }
        m_logger.debug("Exit unregister");
    }

    /**
     * Generate unique client identify for each channel
     * @return - the generated identify
     */
    protected String generateClientID()
    {
        if ( m_channel == null )
            return m_listener;
        else
            return m_listener + "__C__" + m_channel;
    }

    /**
     *
     * @return - current listening thread's client identify
     */
    public String getClientID()
    {
        return m_clientID;
    }

    /**
     *
     * @return - current listening thread's listener name
     */
    public String getListenerName()
    {
        return m_listener;
    }

    /**
     *
     * @return - cureent listening thread's channel name
     */
    public String getChannelName()
    {
        return m_channel;
    }

    /**
     *
     * @return - the JMS message selector string from the registered listening list
     */
    protected String getSelectorString()
    {
        // Check merchant id
        String mstr = ListeningUtils.JMS_MERCHANT_PROPERTY + " in (";
        boolean mflag = false;
        String sid;
        String pstr ="";

        m_logger.debug("Enter getSelectorString");

        for ( Iterator lt = m_merchantIDs.iterator(); lt.hasNext(); )
        {
            sid = (String)lt.next();
            mstr = mstr.concat( pstr + "'" + sid + "'" );
            if ( !mflag )
            {
                pstr = ", ";
                mflag = true;
            }
        }
        if ( mflag )
            mstr = mstr.concat(")");

        // Just check message type without version
        String tstr = ListeningUtils.JMS_MESSAGE_TYPE + " in (";
        boolean tflag = false;
        ListeningMessageBean msg;
        String version, type;

        if ( m_messages == null || m_messages.size() == 0
             || ListeningUtils.containAllMessages( m_messages ) )
        {
            if ( mflag )
                return mstr;
            else
                return "";
        }

        ArrayList msgList = new ArrayList();
        pstr = "";
        for ( Iterator lt = m_messages.values().iterator(); lt.hasNext(); )
        {
            msg = (ListeningMessageBean)lt.next();
            version = msg.getVersion();
            if ( version != null && !version.trim().equals("") )
                continue;

            type = msg.getType();
            msgList.add( type );
            tstr = tstr.concat( pstr + "'" + type + "'");
            if ( !tflag )
            {
                tflag = true;
                pstr = ", ";
            }
        }
        if ( tflag )
            tstr = tstr.concat(")");
        m_logger.debug("mstr=["+mstr+"] tstr=["+tstr+"]");

        // Check messageType && version
        String vstr = "";
        boolean vflag = false;
        pstr = "";
        for ( Iterator lt = m_messages.values().iterator(); lt.hasNext(); )
        {
            msg = (ListeningMessageBean)lt.next();
            version = msg.getVersion();
            type = msg.getType();
            if ( version == null || version.trim().equals("") || msgList.contains(type) )
                continue;

            vstr = vstr.concat( pstr + " (" + ListeningUtils.JMS_MESSAGE_TYPE
                               + " = '" + type + "' AND "
                               + ListeningUtils.JMS_MESSAGE_VERSION
                               + " = '" + version + "') " );
            if ( !vflag )
            {
                vflag = true;
                pstr = " OR";
            }

        }

        m_logger.debug("vstr=["+vstr+"]");

        boolean sflag = false;
        String sstr = "";

        if ( tflag && vflag )
        {
            sflag = true;
            sstr = tstr + " OR " + vstr ;
        }
        else if ( tflag )
        {
            sflag = true;
            sstr = tstr;
        }
        else if ( vflag )
        {
            sflag = true;
            sstr = vstr ;
        }


        if ( mflag && sflag )
            return mstr.concat(" AND (").concat(sstr).concat(")");
        else if (mflag)
            return mstr;
        else if (sflag)
            return sstr;
        return null;
    }

    /**
     * Get XML document from the given string
     * @param msg - message string
     * @return - XML document
     */
    protected Document getXmlDoc( String msg )
    {
        return XMLUtils.getDocumentFromString(msg);
    }

    /**
     * Transform message body to XML document and encrypt/mask its fields.
     * @param type - message type
     * @param version - message version
     * @param doc - XML document
     * @param props - message data that will be saved directly into the database
     */
    protected Document transformMsg( String type, String version,
                                     Document doc, Properties props )
    {
        m_logger.debug("Enter transformMsg");

        MessageColumnDataExtract.getMessageColumnData( props,
                type, version, doc );

        List items = null;
        ListeningMessageBean bean = BeansHelper.retriveMessage( m_messages, type, version );
        if ( bean != null )
            items = bean.getFields();

        List encryptedList = Collections.synchronizedList( new ArrayList() );
        List maskedList = Collections.synchronizedList( new ArrayList() );

        if ( items != null )
        {
            if ( m_plugin == null || m_plugin.acceptFilter() )
                MessageFieldsFilter.extractFields( type, version, items, doc );
            MessageFieldsFilter.maskFields( type, version, items, doc );
            MessageFieldsFilter.encryptFields( type, version, items, doc );
        }

        MessageFieldsFilter.doMustEncrypt( items, doc, type, version );
        MessageColumnDataExtract.checkCardNumberField( props, type, version,
                doc, items );

        m_logger.debug("Exit transformMsg");
        return doc;
    }

    /**
     * This happened after the listener is stopped and the new messages map
     * and merchant ids are set
     */
    protected void changeFilter()
    {
        m_logger.debug("Enter changeFilter");

        if ( m_plugin != null && ( m_plugin.acceptFilterChange() || m_plugin.acceptDynamicFilterChange()) )
        {
            try {
                if ( !m_plugin.acceptDynamicFilterChange() )
                    m_session.unsubscribe( m_clientID );

                m_connection.close();
                m_initFlag = false;
                initializeJMS();

                m_plugin.setFilter( ListeningUtils.createFilterProperties(
                        m_messages, m_merchantIDs) );

//                String sstr = getSelectorString();
//                m_logger.debug("Selector String is [" + sstr + "]");
//              m_sub = m_session.createDurableSubscriber(m_topic, m_clientID );
//                m_sub = m_session.createDurableSubscriber(m_topic, m_clientID,
//                        sstr, true );
//                m_sub.setMessageListener(this);
                m_logger.debug("ChangeFilter successfully.");

            } catch (Throwable t) {
                m_logger.error("In changeFilter", t);
            }

        }

        m_logger.debug("Exit changeFilter");
    }

    /**
     * Set messages map list
     * @param messages - messages map list
     */
    public void setMessagesMap( Map messages )
    {
        m_messages = BeansHelper.cloneListeningMessageMap( messages );
    }

    /**
     *
     * @return - messages map list
     */
    public Map getMessagesMap()
    {
        return m_messages;
    }

    /**
     * set merchant ID list
     * @param ids - merchant ID list.
     */
    public void setMerchantIDs( List ids )
    {
        m_merchantIDs = BeansHelper.cloneMerchantList( ids );
    }

    /**
     *
     * @return - merchant ID list
     */
    public List getMerchantIDs()
    {
        return m_merchantIDs;
    }

	/**
	*
	* @return the long value of startStopTime.
	*/
	public long getStartStopTime(){
		return startStopTime;
	}

	/**
	*
	* @param aStartStopTime - the new value for startStopTime
	*/
	public void setStartStopTime(long aStartStopTime){
		startStopTime = aStartStopTime;
	}


	/**
	*
	* @return the String value of startStopReason.
	*/
	public String getStartStopReason(){
		return startStopReason;
	}

	/**
	*
	* @param aStartStopReason - the new value for startStopReason
	*/
	public void setStartStopReason(String aStartStopReason){
		setStartStopReason( Utils.LISTENING_START_STOP_BY_SERVER,
						   aStartStopReason );
	}

	/**
	*
	* @param aStartStopReason - the new value for startStopReason
	*/
	public void setStartStopReason( int id, String aStartStopReason){
		startStopReason = id + Utils.REASON_SUFFIX + aStartStopReason;
	}

	/**
	*
	* @param aStartStopReason - the  startStopReason ID
	*/
	public void setStartStopReason( int aStartStopReason ){
		startStopReason = aStartStopReason + Utils.REASON_SUFFIX;
	}
}