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

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Iterator;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;
/**
 * <p>Title: PluginServiceBean</p>
 * <p>Description: Hold the Plugin Service related data </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class PluginServiceBean implements Serializable, ConfigureConstants
{

    private Map plugins = Collections.synchronizedMap( new TreeMap() );

    /**
     * Default constructor
     */
    public PluginServiceBean()
    {
    }

    /**
     * Add one plugin listener
     * @param listener - a plugin listener bean
     */
    public void AddPlugin( PluginListenerBean listener )
    {
        if ( listener != null )
            plugins.put( listener.getId(), listener );
    }

    /**
     * Remove one plugin listener
     * @param listener - a plugin listener bean
     */
    public void removePlugin(  PluginListenerBean listener )
    {
        if ( listener != null )
            plugins.remove( listener.getId() );
    }

    /**
     * Remove one plugin listener
     * @param listener - a plugin listener bean identify
     */
    public void removePlugin(  String listener )
    {
        if ( listener != null )
            plugins.remove( listener );
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.pluginServiceFromXml( this, xml );
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
       if ( plugins == null )
        {
            System.out.println("No listening define yet.");
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("\r\n\t<");
        sb.append( PLUGIN_SERVICE_TAG );
        sb.append(">\r\n");

        PluginListenerBean bean;

        for ( Iterator lt = plugins.values().iterator(); lt.hasNext(); )
        {
             bean = (PluginListenerBean)lt.next();
             sb.append( bean.toXml() ) ;
        }

        sb.append("\t</");
        sb.append( PLUGIN_SERVICE_TAG );
        sb.append(">\r\n");

        return sb.toString();
    }

    /**
    *
    * @return the Map value of plugins.
    */
    public Map getPlugins(){
        return plugins;
    }

    /**
    *
    * @param aPlugins - the new value for plugins
    */
    public void setPlugins(Map aPlugins){
        plugins = aPlugins;
    }

    /**
     * Get a plugin listener bean
     * @param id - plugin listener identify
     * @return - the plugin listener bean
     */
    public PluginListenerBean getPluginListener( String id )
    {
        if ( plugins == null )
            return null;
        return (PluginListenerBean) plugins.get( id );
    }

}