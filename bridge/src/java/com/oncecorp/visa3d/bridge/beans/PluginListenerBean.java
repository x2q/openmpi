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
import java.util.TreeMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * <p>Title: PluginListenerBean</p>
 * <p>Description: Hold the Plugin Listener related data </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class PluginListenerBean implements Serializable, ConfigureConstants
{
    private String className = null;
    private String id = null;
    private Map messages = Collections.synchronizedMap( new TreeMap() );
    private Map channels = Collections.synchronizedMap( new TreeMap() );

    /**
     * Default constructor
     */
    public PluginListenerBean()
    {
    }

    /**
     * Constructor with id and classname
     * @param id - plugin identify
     * @param classname - plugin classname
     */
    public PluginListenerBean( String id, String classname )
    {
        setId( id );
        setClassName( classname );
    }

    /**
    *
    * @return the String value of className.
    */
    public String getClassName(){
        return className;
    }

    /**
    *
    * @param aClassName - the new value for className
    */
    public void setClassName(String aClassName){
        className = aClassName;
    }


    /**
    *
    * @return the String value of id.
    */
    public String getId(){
        return id;
    }

    /**
    *
    * @param aId - the new value for id
    */
    public void setId(String aId){
        id = aId;
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


    /**
     * Add a message type to the listening list
     * @param msg - a listening message
     */
    public void addMessage(  ListeningMessageBean msg )
    {
        BeansHelper.addMessage( messages, msg );
    }

    /**
     * Remove a message type from the listening list
     * @param msg - a listening message
     */
    public void removeMessage( ListeningMessageBean msg )
    {
        BeansHelper.removeMessage( messages, msg );
    }

    /**
     * Remove a message type from the listening list
     * @param type - the message type
     */
    public void removeMessage( String type )
    {
        BeansHelper.removeMessage( messages, type );
    }

    /**
     * Remove a given message type and version from the listening list
     * @param type - message type
     * @param version - message version
     */
    public void removeMessage( String type, String version )
    {
        BeansHelper.removeMessage( messages, type, version );
    }

    /**
    *
    * @return the Map value of channels.
    */
    public Map getChannels(){
        return channels;
    }

    /**
    *
    * @param aChannels - the new value for channels
    */
    public void setChannels(Map aChannels){
        channels = aChannels;
    }

    /**
     * Add one listening channel
     * @param channel - a plugin listener channel
     */
    public void addChannel( PluginChannelBean channel )
    {
        if ( channel != null )
            channels.put( channel.getId(), channel );
    }

    /**
     * Remove a plugin listener channel
     * @param channel - a plugin listener channel
     */
    public void removeChannel( PluginChannelBean channel )
    {
        if ( channel != null )
            channels.remove( channel.getId() );
    }

    /**
     * Remove a plugin listener channel
     * @param channel - a plugin listener channel identify
     */
    public void removeChannel( String channel )
    {
        if ( channel != null )
            channels.remove( channel );
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.pluginListenerFromXml( this, xml );
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("\r\n\t\t<");
        sb.append( PLUGIN_TAG );
        sb.append(" id=\"");
        sb.append( id );
        if ( className != null && !className.trim().equals("") )
            sb.append("\" classname=\"" + className + "\"");
        sb.append(">\r\n");
        String str2 = BeansHelper.message2Xml( messages, "\t\t\t" );
        if ( str2 != null )
            sb.append( str2 + "\r\n");

        PluginChannelBean bean;
        for ( Iterator clt = channels.values().iterator(); clt.hasNext(); )
        {
            bean = (PluginChannelBean) clt.next();
            sb.append( bean.toXml() );
        }
        sb.append("\t\t</");
        sb.append( PLUGIN_TAG );
        sb.append(">\r\n");

        return sb.toString();
    }

}