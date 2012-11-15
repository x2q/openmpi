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

import java.util.Properties;
import java.io.Serializable;

import com.oncecorp.visa3d.bridge.monitoring.ListeningMBean;
import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;

/**
 * <p>Title: Listening </p>
 * <p>Description: Listening service related attributes and operations published to JMX server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class Listening implements Serializable, ListeningMBean
{

    /**
     * This is a generice function to handle listening service related actions
     * @param props - the action related properties
     */
    public void doAction( Properties props )
    {
        if ( ListeningManager.getInstance().doAction( props ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

    /**
     * Start a listener channel
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
	 * @param reason - start reason
     */
    public void startListener( String listener, String channel, String reason )
    {
        if ( ListeningManager.getInstance().startListener( listener, channel, reason ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

    /**
     * Stop a listener channel
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
	 * @param reason - stop reason
     */
    public void stopListener( String listener, String channel, String reason )
    {
        if ( ListeningManager.getInstance().stopListener( listener, channel, reason ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

	/**
	 * Obtain the start stop reason
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 */
	public String obtainStartStopReason( String listener, String channel )
	{
		return ListeningManager.getInstance().getStartStopReason( listener, channel );
	}

	/**
	 * Obtain the start stop time
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 */
	public long obtainStartStopTime( String listener, String channel )
	{
		return ListeningManager.getInstance().getStartStopTime( listener, channel );
	}

    /**
     * Register a listener channel or plug in
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
     * @param xml - the plugin related properties which is defined in a XML string
     */
    public void registerListener( String listener, String channel, String xml )
    {
        if ( ListeningManager.getInstance().registerListener( listener, channel, xml ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

    /**
     * Unregister a listener channel or plugin
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
     */
    public void unregisterListener( String listener, String channel )
    {
        if ( ListeningManager.getInstance().unregisterListener( listener, channel ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

    /**
     *
     * @return - the definition of all the plugins
     */
    public String obtainPluginsXml()
    {
        return ListeningManager.getInstance().getPluginService().toXml();
    }

    /**
     * Get the a channel's start/stop status
     * @param listenr - the plugin listener
     * @param channel - the channel under the given listener
     * @return - the defined plugins status: start/stop
     */
    public String obtainListenerStatus( String listener, String channel )
    {
        return ListeningManager.getInstance().getListenerStatus( listener, channel );
    }

    /**
     * The new listener is defined in the XML string
     * @param xml - XML definition for the listener
     */
    public void updatePluginListener( String xml )
    {
        if ( ListeningManager.getInstance().updatePluginListener(xml) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }

    /**
     * The new channel is defined in the XML string
     * @param listener - plug in listener identify
     * @param xml - XML definition for the channel
     */
    public void updatePluginChannel( String listener, String xml )
    {
        if ( ListeningManager.getInstance().updatePluginChannel( listener, xml ) )
            ConfigurationManager.getInstance().checkAndDoAutoSave();
    }


}