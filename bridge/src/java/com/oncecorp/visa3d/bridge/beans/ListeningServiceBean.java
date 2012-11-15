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

package com.oncecorp.visa3d.bridge.beans;

import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * <p>Title: ListeningServiceBean</p>
 * <p>Description: Listening Service related configuration issues</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningServiceBean implements Serializable, ConfigureConstants
{

    private Map properties = null;

    private long timeServiceInterval = 3 * 60;

    /**
     * Default constructor
     */
    public ListeningServiceBean()
    {
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.listeningServiceFromXml( this, xml );
    }

   /**
    * Generate XML string
    * @return - XML definition
    */
   public String toXml()
   {
         StringBuffer sb = new StringBuffer();

         sb.append("\r\n\t<");
         sb.append( LISTENING_SERVICE_TAG );
         sb.append( " timeServiceInterval=\"");
         sb.append( timeServiceInterval );
         sb.append("\">\r\n");

         if ( properties != null )
         {
             String key, value;
             for ( Iterator lt = properties.keySet().iterator(); lt.hasNext(); )
             {
                 key = (String) lt.next();
                 value = (String) properties.get(key);
                 sb.append("\t\t <" + LISTENING_ATTRIBUTE_TAG +" name=\"");
                 sb.append(key);
                 sb.append("\" value=\"");
                 sb.append(value);
                 sb.append("\" />\r\n");
             }
         }
         sb.append("\t</" + LISTENING_SERVICE_TAG + ">\r\n");
         return sb.toString();
    }

    /**
    *
    * @return the HashMap value of properties.
    */
    public Map getProperties(){
        return properties;
    }

    /**
    *
    * @param aProperties - the new value for properties
    */
    public void setProperties(Map aProperties){
        properties = aProperties;
    }

    /**
    *
    * @return the long value of timeServiceInterval.
    */
    public long getTimeServiceInterval(){
        return timeServiceInterval;
    }

    /**
    *
    * @param aTimeServiceInterval - the new value for timeServiceInterval
    */
    public void setTimeServiceInterval(long aTimeServiceInterval){
        timeServiceInterval = aTimeServiceInterval;
    }



}