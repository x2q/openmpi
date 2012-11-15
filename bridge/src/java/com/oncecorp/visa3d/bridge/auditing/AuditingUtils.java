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


package  com.oncecorp.visa3d.bridge.auditing;

import  java.util.Collections;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.Map;
import  java.util.List;
import  java.util.ArrayList;
import  java.util.Properties;
import  java.util.StringTokenizer;
import  javax.mail.Address;
import  javax.mail.Message;
import  javax.mail.Session;
import  javax.mail.Transport;
import  javax.mail.internet.InternetAddress;
import  javax.mail.internet.MimeMessage;
import  javax.naming.Context;
import  javax.sql.DataSource;
import  org.w3c.dom.Document;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  org.apache.log4j.Logger;
import  com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import  com.oncecorp.visa3d.bridge.beans.MessageDefinitionBean;
import  com.oncecorp.visa3d.bridge.beans.MessageMappingBean;
import  com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import  com.oncecorp.visa3d.bridge.startup.StartupProxy;
import  com.oncecorp.visa3d.bridge.utility.JdbcUtils;
import  com.oncecorp.visa3d.bridge.utility.XMLUtils;
import  com.oncecorp.visa3d.bridge.beans.BeansHelper;


/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class provides the utility methods used in the
 * auditing service.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @author Gang Wu [ gwu@oncecorp.com ]
 * @version 1.0
 */
public class AuditingUtils {
    private static final Logger log4j = DataBridgeLoger.getLogger(AuditingUtils.class);
    /**
     * The <tt>logger id</tt> replacement tag string.
     */
    public static final String TAG_LOG_ID = "%logId%";
    /**
     * The <tt>threshold</tt> replacement tag string.
     */
    public static final String TAG_THRESHOLD_NUMBER = "%thresholdNumber%";
    /**
     * The <tt>maximum number</tt> replacement tag string.
     */
    public static final String TAG_MAXIMUM_NUMBER = "%maximumNumber%";
    /**
     * The <tt>current number</tt> replacement tag string.
     */
    public static final String TAG_CURRENT_NUMBER = "%currentNumber%";
    /**
     * The <tt>timestamp</tt> replacement tag string.
     */
    public static final String TAG_TIMESTAMP = "%timestamp%";
    /**
     * The <tt>message type</tt> replacement tag string.
     */
    public static final String TAG_MESSAGE_TYPE = "%messageType%";
    /**
     * The <tt>data source jndi name</tt> replacement tag string.
     */
    public static final String TAG_JNDI_NAME = "%jndiName%";
	/**
	 * The <tt>Log handler name</tt> replacement tag string.
	 */
	public static final String TAG_LOG_NAME = "%logName%";

    /**
     * The <tt>database schema name</tt> replacement tag string.
     */
    public static final String TAG_SCHEMA_NAME = "%schemaName%";
    /**
     * The <tt>subject</tt> tag used in template xml file.
     */
    public static final String TAG_SUBJECT = "<subject>";
    /**
     * The <tt>body</tt> tag used in template xml file.
     */
    public static final String TAG_BODY = "<body>";
    private static List m_messageTypeList = null;
    public final static String MESSAGE_TABLE_NAME = "MESSAGES";
    public final static String MYSQL_CREATE_MESSAGE_TABLE_SCRIPT = "(" + "message_id VARCHAR(50) NOT NULL,"
            + "merchant_id VARCHAR(24) NOT NULL," + "message_type VARCHAR(18) NOT NULL,"
            + "message_version VARCHAR(12)," + "auth_protocol TINYINT," + "message_status CHAR(1),"
            + "card_number VARCHAR(50)," + "card_number_flag TINYINT," + "transaction_id VARCHAR(28),"
            + "time_of_publishing BIGINT NOT NULL," + "message TEXT," + "PRIMARY KEY (merchant_id, message_id, message_type)"
            + ")";
    public final static String ORACLE_CREATE_MESSAGE_TABLE_SCRIPT = "(" + "message_id VARCHAR2(50) NOT NULL,"
            + "merchant_id VARCHAR2(24) NOT NULL," + "message_type VARCHAR2(18) NOT NULL,"
            + "message_version VARCHAR2(12)," + "auth_protocol NUMBER(1)," +
            "message_status CHAR(1)," + "card_number VARCHAR2(50)," + "card_number_flag NUMBER(1),"
            + "transaction_id VARCHAR2(28)," + "time_of_publishing NUMBER(16) NOT NULL,"
            + "message VARCHAR2(4000)," + "PRIMARY KEY (merchant_id, message_id, message_type)"
            + ")";
    public final static String SAVE_MESSAGES_SQL = "(message_id, merchant_id, time_of_publishing, message, "
            + "message_type, message_version, auth_protocol, " + "message_status, card_number, card_number_flag, "
            + "transaction_id ) " + " VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * Wheather the given message type and version is defined in the configuration file
     * @param type - message type
     * @param version - message version
     * @return - flag whether the given message type and verison is defined
     */
    public static boolean isDefined (String type, String version) {
        return  m_messageTypeList.contains(BeansHelper.getMessageMappingKey(type,
                version));
    }

    /**
     * This method is used for replace a part of the string with the new string.
     * @param source The source string to be operated.
     * @param oldStr The replaced string.
     * @param newStr The replacing string.
     * @return The target string.
     */
    public static String stringReplace (String source, String oldStr, String newStr) {
        if (source == null) {
            return  null;
        }
        int pos = source.indexOf(oldStr);
        if (pos != -1) {
            String target = source.substring(0, pos) + newStr;
            if (pos + oldStr.length() == source.length()) {
                return  target;
            }
            else {
                return  target + source.substring(pos + oldStr.length());
            }
        }
        else {
            return  source;
        }
    }

    /**
     * This method returns the element text which has the given name.
     * @param msg The message document object.
     * @param fullName The element full name.
     * @return The element text value.
     */
    public static String getElementText (Document msg, String fullName) {
        Element currentElement = null;
        int i = 0;
        for (StringTokenizer st = new StringTokenizer(fullName, "."); st.hasMoreTokens(); i++) {
            String currentName = st.nextToken();
            NodeList nodes = null;
            if (i == 0) {
                nodes = msg.getElementsByTagName(currentName);
            }
            else {
                nodes = currentElement.getElementsByTagName(currentName);
            }
            if (nodes == null || nodes.getLength() == 0) {
                return  null;
            }
            currentElement = (Element)nodes.item(0);
        }
        return  XMLUtils.getText(currentElement);
    }

    //private static MimeMessage                         message = null;
    /**
     * This method send the mail.
     * @param logger The sending mail auditing logger.
     * @param jndi - data source JNDI name
     * @param schema - data base schema name
     * @param messageType - message type
     * @param num - record number
     */
    public static void sendMail (AuditingLogger logger, String jndi, String schema,
            String messageType, int num) {
        //if ( message == null ) {
        //  Properties systemProperties = System.getProperties();
        //  systemProperties.put("mail.smtp.host", AuditingManager.getInstance().getBean().getMailInet());
        //  message = new MimeMessage(Session.getInstance(systemProperties, null));
        //}
        final String sender = logger.getBean().getSender();
        final String to = logger.getBean().getTo();
        final String cc = logger.getBean().getCc();
        final String subject = logger.getSubject();
        String body = logger.getBody();
        //body = stringReplace(body, TAG_LOG_ID, logger.getId());
        body = stringReplace(body, TAG_MESSAGE_TYPE, messageType);
        body = stringReplace(body, TAG_THRESHOLD_NUMBER, "" + logger.getBean().getThreshold());
        body = stringReplace(body, TAG_MAXIMUM_NUMBER, "" + logger.getBean().getMaxRows());
        body = stringReplace(body, TAG_CURRENT_NUMBER, "" + num);
        body = stringReplace(body, TAG_JNDI_NAME, jndi);
		body = stringReplace(body, TAG_LOG_NAME, logger.getName() );
        body = stringReplace(body, TAG_SCHEMA_NAME, schema);
        body = stringReplace(body, TAG_TIMESTAMP, "" + new Date());
        final String bodyContent = body;
        Runnable runner = new Runnable() {

            /**
             * put your documentation comment here
             */
            public void run () {
                try {
                    //synchronized  (message) {
                    //  message.setFrom(new InternetAddress(sender));
                    //  message.setRecipients(Message.RecipientType.TO,  InternetAddress.parse(to));
                    //  message.setRecipients(Message.RecipientType.CC,  InternetAddress.parse(cc));
                    //  message.setSubject(subject);
                    //  message.setText(bodyContent);
                    //  Transport.send(message);
                    //}
                    sendMail(AuditingManager.getInstance().getBean().getMailInet(),
                            AuditingManager.getInstance().getBean().getMailUsername(),
                            AuditingManager.getInstance().getBean().getMailPasswd(),
                            sender, to, cc, subject, bodyContent);
                    log4j.debug("Mail is sent.");
                } catch (Exception e) {
                    //e.printStackTrace();
                    log4j.error("Mail send failed.", e);
                }
            }
        };
        Thread thread = new Thread(runner);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /**
     * put your documentation comment here
     * @param smtp
     * @param user
     * @param password
     * @param sender
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @exception Exception
     */
    private static void sendMail (String smtp, String user, String password,
            String sender, String to, String cc, String subject, String content) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", smtp);
        props.setProperty("mail.smtp.user", user == null ? "" : user);
        props.setProperty("mail.from", sender);
        Session session = Session.getDefaultInstance(props);
        Address[] addresses = InternetAddress.parse(to + (cc == null ? "" :
                "," + cc));
        //for (int i = 0; i < recAddresses.length; i++) {
        //  System.out.println(recAddresses[i].toString());
        //}
        MimeMessage message = new MimeMessage(session);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        if (cc != null) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        }
        message.setSubject(subject);
        message.setText(content);
        Transport transport = session.getTransport();
        transport.connect(smtp, user, password);
        transport.sendMessage(message, addresses);
        transport.close();
    }

    /**
     * This method first checks if the applocation server allowing dynamically
     * adding jndi data source.  If so, create jndi data source, otherwise, create
     * the client data source and store into <tt>clientDataSourceMap</tt> map.
     * @param bean The bean class contains the merchant info data.
     */
    public static void addDataSource (MerchantInfoBean bean) {
        log4j.debug("call addDataSource()");
        if (bean == null) {
            return;
        }
        if (StartupProxy.canRegisterDataSource()) {
            // create JNDI data source.
            try {
                log4j.debug("start creating data source in application.");
                log4j.debug("jndi = " + bean.getDataSourceJndi());
                log4j.debug("driver = " + bean.getJdbcDriverName());
                log4j.debug("url = " + bean.getDatabaseUrl());
                log4j.debug("user = " + bean.getDatabaseUserName());
                log4j.debug("pw = " + bean.getDatabasePassword());
                StartupProxy.createDataSource(bean.getDataSourceJndi(), bean.getJdbcDriverName(),
                        bean.getDatabaseUrl(), bean.getDatabaseUserName(),
                        bean.getDatabasePassword());
                log4j.debug("data source created in application server.");
                return;
            } catch (Exception e) {
                log4j.warn("JNDI data source create failed. Try to create the client data source.",
                        e);
            }
        }
    }

    /**
     * put your documentation comment here
     * @param bean
     * @return
     */
    public static DataSource checkAndGetDataSource (MerchantInfoBean bean) {
        if (bean == null) {
            log4j.warn("bean is null.");
            return  null;
        }
        String jndi = bean.getDataSourceJndi();
        DataSource datasource = StartupProxy.getJndiDataSource( jndi );
        // re-create data source and get again.
        if (datasource == null) {
            addDataSource(bean);
            datasource = StartupProxy.getGenericDataSource( jndi,
				bean.getJdbcDriverName(), bean.getDatabaseUrl(),
			    bean.getDatabaseUserName(), bean.getDatabasePassword()	);
        }
        if (datasource == null) {
            log4j.error("Data Source for \"" + jndi + "\" does not exist.");
        }
        return  datasource;
    }

    /**
     * This method remove the data source.
     * @param jndi The data source's jndi name.
     */
    public static void removeDataSource (String jndi)
	{
        StartupProxy.removeDataSource(jndi);
    }

    /**
     * Called by configuration manager to extract message types list.
     *
     * @param bean - message mapping bean which contains all message definition
     */
    public static void initMessageTable (MessageMappingBean bean) {
        if (bean != null) {
            Map msgs = bean.getMessages();
            m_messageTypeList = Collections.synchronizedList(new ArrayList());
            MessageDefinitionBean msgBean;
            String key;
            for (Iterator lt = msgs.values().iterator(); lt.hasNext();) {
                msgBean = (MessageDefinitionBean)lt.next();
                key = BeansHelper.getMessageMappingKey(msgBean.getType(),
                        msgBean.getVersion());
                if (!m_messageTypeList.contains(key))
                    m_messageTypeList.add(key);
            }
        }
    }

    /**
     * This method returns the string script for the message table creation.
     * @param databaseName The database name.
     * @return the sql string script.
     */
    public static String getMessageTableScript (String databaseName) {
        log4j.debug("database name = " + databaseName);
        databaseName = databaseName.toLowerCase();
        if (databaseName.indexOf("mysql") != -1)
            return  MYSQL_CREATE_MESSAGE_TABLE_SCRIPT;
        else if (databaseName.indexOf("oracle") != -1)
            return  ORACLE_CREATE_MESSAGE_TABLE_SCRIPT;
        else
            return  null;
    }

    /**
     *
     * @return - message definition list
     */
    public static List getMessageMappingList () {
        return  m_messageTypeList;
    }

    /**
     * Increase the mapping item integer value
     */
    public static void increaseMapItemValue( Map props, String key )
    {
        if ( props == null || key == null )
            return;

        Integer value = (Integer)props.get( key );
        if ( value != null )
            props.put( key, new Integer( value.intValue() + 1 ) );
    }

    /**
     *
     * @return - counters mapping divided by protocol
     */
    public static Map getProtocolCounter()
    {
        Map counters = Collections.synchronizedMap( new HashMap() );

        counters.put( ""+BeansHelper.VISA_SUPPORT, new Integer(0) );
        counters.put( ""+BeansHelper.MASTER_SUPPORT, new Integer(0) );
        counters.put( ""+BeansHelper.ALL_SUPPORT, new Integer(0) );

        return counters;
    }

    /**
     * put your documentation comment here
     * @param args
     * @exception Exception
     */
    public static void main (String[] args) throws Exception {
        sendMail("localhost", "test", "test", "info@oncecorp.com", "yge@oncecorp.com",
                null, "Test subject", "Test content.");
        //String[] result = getColumnTypes("DB2");
        //for (int i = 0; i < result.length; i++) {
        //  System.out.println(result[i]);
        //}
        /*
         Method[] methods = AuditingListenerBean.class.getDeclaredMethods();
         ArrayList list = new ArrayList();
         for (int i = 0; i < methods.length; i++) {
         String name = methods[i].getName();
         if ( name.startsWith("get") ) {
         String type = methods[i].getReturnType().getName();
         if ( type.equals("boolean") ||
         type.equals("byte") ||
         type.equals("char") ||
         type.equals("int") ||
         type.equals("long") ||
         type.equals("float") ||
         type.equals("double") ||
         type.equals("java.lang.String")
         ) {
         list.add(methods[i]);
         }
         }
         }*/
    }
}



