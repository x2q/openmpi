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

import  java.util.Properties;
import  java.util.HashMap;
import  java.util.Map;
import  java.util.Collections;
import  java.util.Iterator;
import  java.util.List;

import  com.oncecorp.visa3d.bridge.beans.BeansHelper;
import  com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import  com.oncecorp.visa3d.bridge.listening.ListeningUtils;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class provides the mechanism of counting the acceptable
 * message according to its message type.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class BaseCounter
        implements MessageCountable {
    /**
     * The counter name.
     */
    protected String name;

    /**
     * The counter indicator mapping.
     */
    protected Map indicators = Collections.synchronizedMap( new HashMap() );

    /**
     * The constructor.
     * @param name the name of the counter.
     */
    public BaseCounter (String name)
    {
        this.name = name;
        List msgs = AuditingUtils.getMessageMappingList();
        if ( msgs != null )
        {
            indicators = Collections.synchronizedMap( new HashMap() );
            for ( Iterator lt = msgs.iterator(); lt.hasNext(); )
            {
                indicators.put( lt.next(), AuditingUtils.getProtocolCounter() );
            }
        }
    }

    /**
     * This method returns the name of the counter.
     * @return The name of the counter.
     */
    public String getName () {
        return  this.name;
    }

    /**
     * This method returns the given message type's current counting value.
     * @param name the counter name.
     * @param msgType The message type.
     * @param msgVersion The message version.
     * @return The current counting value.
     */
    public Map getCountingValue (String name, String msgType, String msgVersion)
    {
        if (!this.name.equals(name)) {
            return  Collections.synchronizedMap( new HashMap() );
        }
        Integer value1, value2;
        Map pmap;
        if (PropertiesConstants.ALL_TYPE.equals(msgType)) {
            Map total = AuditingUtils.getProtocolCounter();
            String key;
            for (Iterator lt = indicators.values().iterator(); lt.hasNext();) {
                pmap = (Map)lt.next();
                for ( Iterator lkey = pmap.keySet().iterator(); lkey.hasNext(); )
                {
                    key = (String)lkey.next();
                    value1 = (Integer)pmap.get( key );
                    value2 = (Integer)total.get( key );
                    total.put( key, new Integer(
                            value1.intValue() + value2.intValue() ) );
                }
            }
            return  total;
        }
        String key = BeansHelper.getMessageMappingKey(msgType, msgVersion);
        if (indicators.containsKey(key)) {
            return (Map)indicators.get(key);
        }
        else {
            return  Collections.synchronizedMap( new HashMap() );
        }
    }

    /**
     * This method checks the counting message to see if this message is accepted
     * to count.
     * @param props - The message properties sent form Listening Service.
     * @return The checking result.
     */
    protected boolean accept (Properties props) {
        return  true;
    }

    /**
     * This method counts the message based on the message properties.
     * @param - message properties
     */
    public synchronized void count ( Properties props )
    {
        String msgType = props.getProperty( ListeningUtils.JMS_MESSAGE_TYPE );
        String msgVersion = props.getProperty( ListeningUtils.JMS_MESSAGE_VERSION );
        String key = BeansHelper.getMessageMappingKey( msgType, msgVersion );

        if ( accept(props) && indicators.containsKey(key) )
        {
            String protocol = props.getProperty( ListeningUtils.JMS_MESSAGE_PROTOCOL );
            Map pmap = (Map)indicators.get(key);
            AuditingUtils.increaseMapItemValue( pmap, ""+ BeansHelper.ALL_SUPPORT );
            AuditingUtils.increaseMapItemValue( pmap, protocol );
        }
    }

    /**
     * This method flushes the all held counters.
     */
    public synchronized void flush ()
    {
        if (indicators == null)
            return;
        String key;
        for (Iterator lt = indicators.keySet().iterator(); lt.hasNext();) {
            key = (String)lt.next();
            indicators.put(key, AuditingUtils.getProtocolCounter() );
        }
    }
}



