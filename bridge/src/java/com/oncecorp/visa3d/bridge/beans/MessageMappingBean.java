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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.Iterator;

/**
 * <p>Title: MessageMappingBean</p>
 * <p>Description: Contains all message mapping information. The key is the object of
 * MessageDefinitionBean.The field list is a list of FieldDefinitionBean.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class MessageMappingBean implements Serializable
{

    private Map messages = Collections.synchronizedMap( new TreeMap() );

    /**
     * Default Constructor
     */
    public MessageMappingBean()
    {
    }

    /**
     * Get message definition from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.messageMappingFromXml( this, xml );
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<messageDefiniton>");

        MessageDefinitionBean bean;
        FieldDefinitionBean fdb;

        for ( Iterator lt = messages.values().iterator(); lt.hasNext(); )
        {
            bean = (MessageDefinitionBean) lt.next();
            sb.append("\r\n\t<message type=\"");
            sb.append( bean.getType() );
            sb.append("\" version=\"");
            sb.append(bean.getVersion());
            sb.append("\" xpath=\"");
            sb.append( bean.getXpath() );
            sb.append("\">\r\n");
            for ( Iterator fields = bean.getFields().iterator(); fields.hasNext(); )
            {
                fdb = (FieldDefinitionBean) fields.next();
                sb.append("\t\t<field mandatory=\"");
                sb.append( fdb.isMandatory() );
                sb.append( "\" mustEncryption=\"");
                sb.append( fdb.isMustEncryption() );
                sb.append("\">");
                sb.append( fdb.getXpath() );
                sb.append("</field>\r\n");
            }
            sb.append("\t</message>\r\n");
        }
        sb.append("</messageDefiniton>");

        return sb.toString();
    }

    /**
     * Get Message mapping fields of the given type and version
     * @param type - message type
     * @param version - message version
     * @return - The list of message fields for this message type
     */
    public List getFieldsList( String type, String version )
    {
        MessageDefinitionBean bean = (MessageDefinitionBean)
              messages.get( BeansHelper.getMessageMappingKey( type, version ) );
        if ( bean != null )
            return bean.getFields();
        else
            return null;
    }

    /**
     * Get Message definition of the given type and version
     * @param type - message type
     * @param version - message version
     * @return - The message definition
     */
    public MessageDefinitionBean getMessageDefinition( String type, String version )
    {
        return (MessageDefinitionBean)
              messages.get( BeansHelper.getMessageMappingKey( type, version ) );
    }

    /**
     * Add one message item to the list
     * @param type - message type
     * @param version - message version
     * @param fields - messsage fields list
     */
    public void addMessageItem( MessageDefinitionBean bean )
    {
        if ( bean == null )
            return;
        String key = BeansHelper.getMessageMappingKey( bean.getType(), bean.getVersion() );
        messages.put( key, bean );
    }

    /**
     * Remove one message item from the list
     * @param type - message type
     * @param version - message version
     */
    public void removeMessageItem( String type, String version )
    {
        String key = BeansHelper.getMessageMappingKey( type, version );
        if ( messages.containsKey( key ) )
            messages.remove( key );
    }

    /**
    *
    * @return the Map value of messages.
    */
    public Map getMessages(){
        return messages;
    }

    /**
    *
    * @param aMessages - the new value for messages
    */
    public void setMessages(Map aMessages){
        messages = aMessages;
    }


}