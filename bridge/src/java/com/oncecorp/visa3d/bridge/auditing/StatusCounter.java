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
import  java.util.ArrayList;
import  java.util.Collections;
import  java.util.Iterator;
import  java.util.List;
import  java.util.Map;
import  java.util.HashMap;

import  com.oncecorp.visa3d.bridge.listening.ListeningUtils;
import  com.oncecorp.visa3d.bridge.beans.BeansHelper;
import  com.oncecorp.visa3d.bridge.listening.MessageColumnDataExtract;
import  com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This is the status counter class.  In the class, the condition
 * map is encapsulated which contains the name-value pairs.
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class StatusCounter    implements MessageCountable
{
    private List conditionList;
    private String name;
    private Map counters;

    /**
     * The constructor of the class.
     */
    public StatusCounter (String name) {
        this.name = name;
        conditionList = Collections.synchronizedList(new ArrayList());
        counters = AuditingUtils.getProtocolCounter();
    }

    /**
     * This method adds the name-value pair into the condition pair list.
     * @param type  - The message type.
     * @param status - The status value.
     */
    public void addConditionPair (String type, String status ) {
        conditionList.add( type + ConfigureConstants.MESSAGE_KEY_DELIMITER + status );
    }

    /**
     * This method checks the counting message to see if this message is accepted
     * to count.
     * @param props - The message properties sent form Listening Service.
     * @return The checking result.
     */
    protected boolean accept (Properties props)
    {
        String status = props.getProperty( MessageColumnDataExtract.MESSAGE_STATUS );
        String type = props.getProperty( ListeningUtils.JMS_MESSAGE_TYPE );
        if ( status == null || type == null  )
            return false;

        return conditionList.contains( type + ConfigureConstants.MESSAGE_KEY_DELIMITER + status );
    }

    /**
     * This method returns the name of the counter.
     * @return The name of the counter.
     */
    public String getName()
    {
        return  this.name;
    }

    /**
     * This method returns the given message type's current counting value.
     * @param name the counter name.
     * @param msgType - The message type.
     * @param msgVersion - The message version.
     * @return The current counting value.
     */
    public Map getCountingValue(String name, String msgType, String msgVersion)
    {
        if (!this.name.equals(name))
            return  Collections.synchronizedMap( new HashMap() );
        else
            return counters;
    }

    /**
     * This method counts the message based on the message properties.
     * @param - message properties
     */
    public void count( Properties props )
    {
        if ( accept( props ) )
        {
            String protocol = props.getProperty( ListeningUtils.JMS_MESSAGE_PROTOCOL );
            AuditingUtils.increaseMapItemValue( counters, ""+ BeansHelper.ALL_SUPPORT );
            AuditingUtils.increaseMapItemValue( counters, protocol );
        }
    }

    /**
     * This method flushes the all held counters.
     */
    public void flush()
    {
        counters = AuditingUtils.getProtocolCounter();
    }
}



