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

import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.Message;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.ListeningMessageBean;
import com.oncecorp.visa3d.bridge.beans.MessageFieldBean;
import com.oncecorp.visa3d.bridge.beans.MessageMappingBean;

/**
 * <p>Title: ListeningUtils</p>
 * <p>Description: Helper class for Listening Service </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningUtils
{
    public final static String AUDITING_LISTENER  =  "AuditingListener";
    public final static String PLUGIN_LISTENER    =  "PluginListener";
    public final static String LISTENER    =  "Listener";
    public final static String CHANNEL    =  "Channel";
    public final static String ACTION          = "action";
    public final static String START            = "start";
    public final static String STOP            = "stop";
	public final static String REASON            = "reason";
    public final static String EXCEPTION         = "exception";
    public final static String PLUGIN_CLASS_NOT_INIT         = "pluginClassCannotInstanitiated";
    public final static String INITIALIZE_ERROR         = "initializeError";
    public final static String REGISTER            = "register";
    public final static String UNREGISTER            = "unregister";
    public final static String PLUG_NAME            = "pluginName";
    public final static String CLASS_NAME            = "className";
    public final static String CONFIG_XML            = "configXml";
    public final static String JMS_MERCHANT_PROPERTY = "MerchantID";
    public final static String JMS_MESSAGE_TYPE      = "MessageType";
    public final static String JMS_MESSAGE_VERSION   = "MessageVersion";
    public final static String JMS_MESSAGE_PROTOCOL   = "Protocol";
    public final static String JMS_MESSAGE_TIMESTAMP   = "MessageTimestamp";
    public final static String JMS_ENCRYPTION_MODE   = "EncryptionMode";
    public final static String JMS_ENCRYPTION_IV   = "EncryptionIV";

    private static Logger m_logger = DataBridgeLoger.getLogger(
            ListeningUtils.class.getName() );

    /**
     * Default constructor
     */
    public ListeningUtils()
    {
    }

    /**
     * Extract action string from property list
     * @param props - property list
     * @return - the action string
     */
    public static String getAction( Properties props )
    {
        if ( props != null && props.containsKey(ACTION) )
            return props.getProperty(ACTION);
        return null;
    }

    /**
     * Extract listener name from property list
     * @param props - property list
     * @return - the listener name
     */
    public static String getListener( Properties props )
    {
        if ( props != null && props.containsKey(LISTENER) )
            return props.getProperty(LISTENER);
        return null;
    }

	/**
	 * Extract reason from property list
	 * @param props - property list
	 * @return - the start/stop reason
	 */
	public static String getReason( Properties props )
	{
		if ( props != null && props.containsKey(REASON) )
			return props.getProperty(REASON);
		return "";
	}

    /**
     * Extrace channel name from property list
     * @param props - propery list
     * @return - the channel name
     */
    public static String getChannel( Properties props )
    {
        if ( props != null && props.containsKey(CHANNEL) )
            return props.getProperty(CHANNEL);
        return null;
    }

    /**
     * Extract xml configue string from property list
     * @param props - property list
     * @return - xml configue string
     */
    public static String getConfigXml( Properties props )
    {
        if ( props != null && props.containsKey(CONFIG_XML) )
            return props.getProperty(CONFIG_XML);
        return null;
    }

    /**
     * Extract listener class name from property list
     * @param props - property list
     * @return - listener class name
     */
    public static String getPluginClass( Properties props )
    {
        if ( props != null && props.containsKey(CLASS_NAME) )
            return props.getProperty(CLASS_NAME);
        return null;
    }

    /**
     * Is given action string a stop action?
     * @param action - an action string
     * @return - true or false
     */
    public static boolean isStopAction( String action )
    {
        if ( action != null && action.trim().equalsIgnoreCase(STOP) )
            return true;
        else
            return false;
    }

    /**
     * Is given action string a start action?
     * @param action - an action string
     * @return - true or false
     */
    public static boolean isStartAction( String action )
    {
        if ( action != null && action.trim().equalsIgnoreCase(START) )
            return true;
        else
            return false;
    }

    /**
     * Is given action string a register action?
     * @param action - an action string
     * @return - true or false
     */
    public static boolean isRegisterAction( String action )
    {
        if ( action != null && action.trim().equalsIgnoreCase(REGISTER) )
            return true;
        else
            return false;
    }

    /**
     * The given action should stop the listening thread
     * @param action - an action string
     * @return - true or false
     */
    public static boolean shouldStop( String action )
    {
        if ( action != null &&
          ( action.trim().equalsIgnoreCase( STOP )
          || action.trim().equalsIgnoreCase( UNREGISTER ) ) )
            return true;
        else
            return false;


    }

    /**
     * Is given action string a unregister action?
     * @param action - an action string
     * @return - true or false
     */
    public static boolean isUnregisterAction( String action )
    {
        if ( action != null && action.trim().equalsIgnoreCase(UNREGISTER) )
            return true;
        else
            return false;
    }

    /**
     * Is given listener name a auditing listener?
     * @param listener - a listener name
     * @return - true or false
     */
    public static boolean isAuditingListener( String listener )
    {
        if ( listener != null )
            return listener.equals( AUDITING_LISTENER );

        return false;
    }

    /**
     * Get published JMS related head properties
     * @param message - JMS message
     * @return - all published JMS related head properties
     */
    public static Properties getMessageProperties( Message message )
    {
        Properties props = new Properties();

        try {
            String str = message.getStringProperty( JMS_MERCHANT_PROPERTY );
            if ( str != null )
                props.put( JMS_MERCHANT_PROPERTY, str );
        } catch ( Exception e )
        {
            m_logger.error("Get JMS_MERCHANT_PROPERTY", e);
        }

        try {
            String str = message.getStringProperty( JMS_MESSAGE_TYPE );
            if ( str != null )
                props.put( JMS_MESSAGE_TYPE, str );
        } catch ( Exception e )
        {
            m_logger.error("Get JMS_MESSAGE_TYPE", e);
        }

        try {
            String str = message.getStringProperty( JMS_MESSAGE_VERSION );
            if ( str != null )
                props.put( JMS_MESSAGE_VERSION, str );
        } catch ( Exception e )
        {
            m_logger.error("Get JMS_MESSAGE_VERSION", e);
        }

        try {
            String str = message.getStringProperty( JMS_MESSAGE_PROTOCOL );
            if ( str != null )
                props.put( JMS_MESSAGE_PROTOCOL, str );
        } catch ( Exception e )
        {
            m_logger.error("Get JMS_MESSAGE_PROTOCOL", e);
        }

        try {
            props.put( JMS_MESSAGE_TIMESTAMP, ""+message.getJMSTimestamp() );
        } catch ( Exception e )
        {
            m_logger.error("Get JMS_MESSAGE_TIMESTAMP", e);
        }

        return props;
    }

    /**
     * Iterate through two merchant lists and check whether they are equal
     * @param l1 - first merchant list
     * @param l2 - second merchant list
     * @return - equal or not
     */
    public static boolean merchantListEqual( List l1, List l2 )
    {
        if ( l1 == null && l2 == null )
            return true;
        else if ( l1 == null && l2 != null )
            return false;
        else if ( l1 != null && l2 == null )
            return false;
        else if ( l1.size() != l2.size() )
            return false;

        for ( Iterator lt1 = l1.iterator(); lt1.hasNext(); )
        {
            if ( !l2.contains( lt1.next() ) )
                return false;
        }

        return true;
    }


    /**
     * Iterate through two fields lists and check whether they are equal
     * @param l1 - first field list
     * @param l2 - second field list
     * @return - equal or not
     */
    public static boolean fieldsListEqual( List l1, List l2 )
    {
        MessageFieldBean mfb1, mfb2;

        if ( l1 == null && l2 == null )
            return true;
        else if ( l1 == null && l2 != null )
            return false;
        else if ( l1 != null && l2 == null )
            return false;
        else if ( l1.size() != l2.size() )
            return false;

        String name, format;
        boolean eflag, efound;

        for ( Iterator lt1 = l1.iterator(); lt1.hasNext(); )
        {
            mfb1 = (MessageFieldBean) lt1.next();
            name = mfb1.getName();
            format = mfb1.getFormat();
            eflag = mfb1.isEncryption();

            efound = false;
            for ( Iterator lt2 = l2.iterator(); lt2.hasNext(); )
            {
                mfb2 = (MessageFieldBean) lt2.next();

                if ( !stringEqual( name, mfb2.getName() ) )
                    continue;
                if ( !stringEqual( format, mfb2.getFormat() ) )
                    continue;
                if ( !booleanEqual( eflag, mfb2.isEncryption() ) )
                    continue;

                efound = true;
                break;
            }

            if ( !efound )
                return false;
        }

        return true;
    }

    /**
     * Iterate through two message maps and check whether they are equal
     * @param m1 - first message map
     * @param m2 - second message map
     * @param cflag - whether check fields list
     * @return - equal or not
     */
    public static boolean messageMapEqual( Map m1, Map m2, boolean cflag )
    {

        if ( m1 == null && m2 == null )
            return true;
        else if ( m1 == null && m2 != null )
            return false;
        else if ( m1 != null && m2 == null )
            return false;
        else if ( m1.size() != m2.size() )
            return false;

        String key;
        ListeningMessageBean lmb1, lmb2;

        for ( Iterator lt = m1.keySet().iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();
            if ( !m2.containsKey(key) )
                return false;
            if ( cflag )
            {
                lmb1 = (ListeningMessageBean)m1.get(key);
                lmb2 = (ListeningMessageBean)m2.get(key);
                if ( !fieldsListEqual( lmb1.getFields(), lmb2.getFields() ) )
                    return false;
            }
        }

        return true;
    }

    /**
     * Check two string object equal or not, which could be null
     * @param s1 - first string
     * @param s2 - second string
     * @return - equal or not
     */
    public static boolean stringEqual( String s1, String s2 )
    {
        if ( s1 == null && s2 == null )
            return true;
        else if ( s1 == null && s2 != null )
            return false;
        else if ( s1 != null && s2 == null )
            return false;
        else
            return s1.equals(s2);
    }

    /**
     * Check two boolean variable equal or not
     * @param b1 - first boolean variable
     * @param b2 - second boolean variable
     * @return - equal or not
     */
    public static boolean booleanEqual( boolean b1, boolean b2 )
    {
        if ( ( b1 && b2 ) || ( !b1 && !b2 ) )
            return true;
        else
            return false;
    }

    /**
     * Generate the version list that defined under the same message type
     * @param msgs - message map
     * @param type - message type
     * @return - version list
     */
    public static ArrayList getMessageVersionList( Map msgs, String type )
    {
        ArrayList al = new ArrayList();
        ListeningMessageBean lmb;
        String version;
        for ( Iterator lt = msgs.values().iterator(); lt.hasNext(); )
        {
            lmb = (ListeningMessageBean) lt.next();
            if ( type.equals( lmb.getType() ) )
            {
                version = lmb.getVersion();
                if ( !al.contains( version ) )
                    al.add( version );
            }
        }

        return al;
    }

    /**
     * Create filter properties that used by Plugins, it include a merchant id list
     * and a message map which the key is the message type and value is a version list
     * that defined under that type
     * @param msgs - message map
     * @param mids - merchant ids
     * @return - filter properties structure
     */
    public static Properties createFilterProperties( Map msgs, List mids )
    {
        Properties props = new Properties();
        props.put( JMS_MERCHANT_PROPERTY, mids );
        HashMap map = new HashMap();
        String key;

        for ( Iterator lt = msgs.keySet().iterator(); lt.hasNext(); )
        {
            key = (String) lt.next();
            map.put( key, getMessageVersionList(msgs, key) );
        }

        props.put( JMS_MESSAGE_TYPE, map );

        return props;
    }

    /**
     * Check wheather the given message mapping contain all defined messages, so
     * we can use this check to tune up the JMS selector string.
     * @param msgs - a given messages mapping
     * @return - contain all defined messages or not
     */
    public static boolean containAllMessages( Map msgs )
    {
        MessageMappingBean mmb = MessageFieldsFilter.getMessageMappingBean();

        if ( mmb == null )
            return true;

        Map definedMsgs = mmb.getMessages();

        if ( msgs == null )
            return true;
        else if ( msgs.size() < definedMsgs.size() )
            return false;

        Object key;

        for ( Iterator lt = definedMsgs.keySet().iterator(); lt.hasNext(); )
        {
            key = lt.next();
            if ( !msgs.containsKey( key ) )
                return false;
        }

        m_logger.debug("contain all messages is true.");
        return true;
    }

}