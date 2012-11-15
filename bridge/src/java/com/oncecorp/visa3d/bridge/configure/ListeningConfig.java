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

package com.oncecorp.visa3d.bridge.configure;

import javax.naming.Context;
import java.util.Map;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.security.TripleDESEncrypter;
import com.oncecorp.visa3d.bridge.listening.TimerService;
import com.oncecorp.visa3d.bridge.beans.ListeningServiceBean;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;
import com.oncecorp.visa3d.bridge.listening.ListeningThread;
import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * <p>Title: ListeningConfig</p>
 * <p>Description: Config Listening Service</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningConfig implements Configurable, ConfigureConstants
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            ListeningConfig.class.getName() );

    private static Map m_props = null;

    /**
     * Default constructor
     */
    public ListeningConfig()
    {
    }

    /**
     * Configure Interface
     * @param root - the new databridge bean
     */
    public void config( DataBridgeBean root )
    {

        m_logger.debug("Enter config");
        ListeningServiceBean lsb = root.getListeningService();

        long interval = lsb.getTimeServiceInterval();
        if ( interval != 0 )
            TimerService.getInstance().setInterval(interval);

        Map props = lsb.getProperties();
        if ( props != null )
        {
            if ( m_props == null )
            {
                resetProperties( props );
            }
            else
            {
                boolean cflag = false;
                String key, value1, value2;
                if ( props.size() != m_props.size() )
                    cflag = true;
                else for ( Iterator lt = props.keySet().iterator(); !cflag && lt.hasNext(); )
                {
                    key = (String) lt.next();
                    value1 = (String)props.get( key );
                    value2 = (String)m_props.get( key );
                    if ( value1 == null && value2 != null )
                        cflag = true;
                    else if ( value1 != null && value2 == null )
                        cflag = true;
                    else if ( value1 != null && value2 != null )
                    {
                        if ( !value1.trim().equals( value2.trim() ) )
                            cflag = true;
                    }
                }
                if ( cflag )
                     resetProperties( props );
            }

            m_props = props;
        }
        else
            m_logger.debug("TripleDESEncrypter properties is null, keep the old one");
        m_logger.debug("Exit config");

   }

   /**
    * reset Listening service related attributes to each package
    * @param props - the attributes(key, value) get from configuration data
    */
   public void resetProperties( Map props )
   {
       m_logger.debug("Enter resetProperties");

       Hashtable hashtable = new Hashtable();
       Map desProps = new TreeMap();

       String key, value;
       String fjndi = null;
       String tjndi = null;

       for ( Iterator lt = props.keySet().iterator(); lt.hasNext(); )
       {
           key = (String)lt.next();
           key = key.trim();

           value = (String)props.get(key);

           if ( value == null )
               continue;
           else
               value = value.trim();

           m_logger.debug("key=[" + key + "] value=[" + value + "]");
           if ( key.equals(JMS_INITIAL_CONTEXT_FACTORY) )
               hashtable.put(Context.INITIAL_CONTEXT_FACTORY, value );
           else if ( key.equals(JMS_PROVIDER_URL) )
               hashtable.put(Context.PROVIDER_URL, value );
           else if ( key.equals(JMS_SECURITY_PRINCIPAL) )
               hashtable.put(Context.SECURITY_PRINCIPAL, value);
           else if ( key.equals(JMS_SECURITY_CREDENTIALS) )
               hashtable.put(Context.SECURITY_CREDENTIALS, value );
           else if ( key.equals(JMS_CONNECTION_FACTORY_JNDI) )
               fjndi = value;
           else if ( key.equals(JMS_TOPIC_JNDI) )
               tjndi = value;
           else if ( key.startsWith( JMS_PREFIX_TAG ) )
               hashtable.put( key.substring( JMS_PREFIX_TAG.length() ), value );
           else if ( key.startsWith( TRIPLEDES_PREFIX_TAG ) )
               desProps.put( key.substring( TRIPLEDES_PREFIX_TAG.length() ), value );
       }

       m_logger.debug("hashtable size=" + hashtable.size());
       m_logger.debug("fjndi=" + fjndi + " tjndi=" + tjndi);
       if ( desProps.size() > 0 )
       try {
           TripleDESEncrypter.initialize( desProps );
       } catch ( Exception e )
       {
           m_logger.debug( "TripleDES Error", e );
       }
       if ( hashtable.size() > 0 )
           ListeningThread.setJMSContextAttributes( hashtable, fjndi, tjndi );

       m_logger.debug("Exit resetProperties");
   }

}