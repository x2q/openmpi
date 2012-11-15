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

/**
 * <p>Title: ListeningManager </p>
 * <p>Description: This Singleton class is responsible start/stop, register/unregister listening thread</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.beans.PluginChannelBean;
import com.oncecorp.visa3d.bridge.beans.PluginListenerBean;
import com.oncecorp.visa3d.bridge.beans.PluginServiceBean;
import com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.auditing.AuditingManager;
import com.oncecorp.visa3d.bridge.utility.Utils;

public class ListeningManager
{
    private static Map m_listeners = Collections.synchronizedMap( new TreeMap() );
    private final static ListeningManager m_instance = new ListeningManager();
    private PluginServiceBean m_pluginServiceBean = null;

    private static Logger m_logger = DataBridgeLoger.getLogger(
            ListeningManager.class.getName() );


    /**
     * Default constructor
     */
    private ListeningManager()
    {
    }

    /**
     * Get the only Listening Manager instance
     * @return - Listening Manager instance
     */
    public static ListeningManager getInstance()
    {
        return m_instance;
    }

    /**
     * This is a generice function to handle listening service related actions
     * @param props - the action related properties
     * @return - successfully action or not
     */
    public boolean doAction(Properties props)
    {

        String action = ListeningUtils.getAction( props );
        String listener = ListeningUtils.getListener(props);
        String channel = ListeningUtils.getChannel(props);
		String reason = ListeningUtils.getReason(props);

        m_logger.debug( "action=[" + action + "] listener=[" + listener + "]");

        if ( action == null || listener == null )
            return false;

        if ( ListeningUtils.isStopAction(action) )
            return stopListener( listener, channel, reason );
        else if ( ListeningUtils.isStartAction(action) )
            return startListener( listener, channel, reason );
        else if ( ListeningUtils.isRegisterAction(action) )
        {
            String xml = ListeningUtils.getConfigXml( props );
            return registerListener( listener, channel, xml );
        }
        else if ( ListeningUtils.isUnregisterAction(action) )
            return unregisterListener( listener, channel );

        return false;
    }

	/**
	 * Start a listener channel
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 * @return - successfully action or not
	 */
	public boolean startListener(String listener, String channel )
	{
		return this.startListener( listener, channel, "" );
	}

	/**
	 * Retrive a listener channel's start/stop reason
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 * @return - start/stop reason
	 */
	public String getStartStopReason( String listener, String channel )
	{
		if ( listener != null )
		{
			Map items = (Map) m_listeners.get( listener );
			if ( items != null && items.size() > 0 )
			{
				if ( channel != null )
				{
					ListeningThread lthread = (ListeningThread) items.get(channel);
					if ( lthread != null )
					{
						return lthread.getStartStopReason();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Retrive a listener channel's start/stop time
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 * @return - start/stop time
	 */
	public long getStartStopTime( String listener, String channel )
	{
		if ( listener != null )
		{
			Map items = (Map) m_listeners.get( listener );
			if ( items != null && items.size() > 0 )
			{
				if ( channel != null )
				{
					ListeningThread lthread = (ListeningThread) items.get(channel);
					if ( lthread != null )
					{
						return lthread.getStartStopTime();
					}
				}
			}
		}
		return 0;
	}

    /**
     * Start a listener channel
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
	 * @param reason - start reason
     * @return - successfully action or not
     */
    public boolean startListener(String listener, String channel, String reason)
    {
        if ( listener != null )
        {
            Map items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                if ( channel != null )
                {
                    m_logger.debug("channel [" + channel + "] is defined" );
                    ListeningThread lthread = (ListeningThread) items.get(channel);
                    if ( lthread != null )
                    {
                        lthread.start( reason );
                        if ( ListeningUtils.isAuditingListener( listener ) )
                        {
                            AuditingMessageReceiver amr = (AuditingMessageReceiver) lthread;
                            amr.setAuditingListenerBeanStatus( Utils.LISTENTING_START );
                        }
                        else
                        {
                            PluginMessageReceiver pmr = (PluginMessageReceiver) lthread;
                            pmr.setPluginChannelBeanStatus( Utils.LISTENTING_START );
                        }
                        return true;
                    }
                }
                else {
                    m_logger.debug("start all channels of listener [" + listener  );
                    for (Iterator lt = items.values().iterator(); lt.hasNext(); )
                    {
                        ListeningThread lthread = (ListeningThread) lt.next();
                        if ( lthread != null )
                        {
                            lthread.start( reason );
                            if ( ListeningUtils.isAuditingListener( listener ) )
                            {
                                AuditingMessageReceiver amr = (AuditingMessageReceiver) lthread;
                                amr.setAuditingListenerBeanStatus( Utils.LISTENTING_START );
                            }
                            else
                            {
                                PluginMessageReceiver pmr = (PluginMessageReceiver) lthread;
                                pmr.setPluginChannelBeanStatus( Utils.LISTENTING_START );
                            }
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

	/**
	 * Stop a listener channel
	 * @param listener - the plugin listener
	 * @param channel - the channel under the given listener
	 * @return - successfully action or not
	 */
	public boolean stopListener(String listener, String channel)
	{
		return this.startListener( listener, channel, "" );
	}

    /**
     * Stop a listener channel
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
	 * @param reason - stop reason
     * @return - successfully action or not
     */
    public boolean stopListener(String listener, String channel, String reason )
    {
        if ( listener != null )
        {
            Map items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                if ( channel != null )
                {
                    m_logger.debug("channel [" + channel + "] is defined" );
                    ListeningThread lthread = (ListeningThread) items.get(channel);
                    if ( lthread != null )
                    {
                        lthread.stop( reason );
                        if ( ListeningUtils.isAuditingListener( listener ) )
                        {
                            AuditingMessageReceiver amr = (AuditingMessageReceiver) lthread;
                            amr.setAuditingListenerBeanStatus( Utils.LISTENING_STOP );
                        }
                        else
                        {
                            PluginMessageReceiver pmr = (PluginMessageReceiver) lthread;
                            pmr.setPluginChannelBeanStatus( Utils.LISTENING_STOP );
                        }
                        return true;
                    }
                }
                else {
                    m_logger.debug("stop all channels of listener [" + listener  );
                    for (Iterator lt = items.values().iterator(); lt.hasNext(); )
                    {
                        ListeningThread lthread = (ListeningThread) lt.next();
                        if ( lthread != null )
                        {
                            lthread.stop( reason );
                            if ( ListeningUtils.isAuditingListener( listener ) )
                            {
                                AuditingMessageReceiver amr = (AuditingMessageReceiver) lthread;
                                amr.setAuditingListenerBeanStatus( Utils.LISTENING_STOP );
                            }
                            else
                            {
                                PluginMessageReceiver pmr = (PluginMessageReceiver) lthread;
                                pmr.setPluginChannelBeanStatus( Utils.LISTENING_STOP );
                            }
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Register a listener channel or plug in
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
     * @param xml - the plugin related properties which is defined in a XML string
     * @return - successfully action or not
     */
    public boolean registerListener(String listener, String channel, String xml)
    {
        if ( listener != null )
        {
            if ( m_listeners.containsKey( listener ) && channel == null )
            {
                m_logger.debug("In [registerPlugin] listener [" + listener + "] is already exists.");
                return false;
            }
            else if ( channel != null )
            {
                Map items = (Map) m_listeners.get( listener );
                if ( items == null )
                {
                    items = Collections.synchronizedMap( new TreeMap() );
                    m_listeners.put( listener, items);
                }
                if ( items.containsKey( channel ) )
                {
                    m_logger.debug("In [registerPlugin] listener [" + listener
                                       + "] channel [" + channel + "] is already exists.");
                    return false;
                }

                if ( !ListeningUtils.isAuditingListener( listener ) )
                {
                    m_logger.debug("Begin register plugin listener ["
                                   + listener + "] channel=[" +  channel + "]" );

                    PluginChannelBean pcb = BeansHelper.pluginChannelFromXml( null, xml );
                    if ( pcb != null )
                    {
                        PluginListenerBean plb = m_pluginServiceBean.getPluginListener( listener );
                        plb.addChannel( pcb );
                        PluginMessageReceiver pmr = new PluginMessageReceiver( pcb, plb );

                        this.registerListener( pmr );

                        m_logger.debug("register successfully.");
                    }
                    else
                    {
                        m_logger.warn("Empy channel from configuration data.");
                    }

                    return true;
                } else
                {
                    m_logger.debug("Begin register auditing listener ["
                                   + listener + "] channel=[" +  channel + "]" );

                    AuditingListenerBean alb = BeansHelper.auditingListenerFromXml( null, xml);
                    if ( alb != null )
                    {
                        AuditingManager.getInstance().addAuditingLogger(alb);
                        AuditingMessageReceiver pmr = new AuditingMessageReceiver( alb );
                        this.registerListener( pmr );
                        m_logger.debug("register successfully.");
                    }
                    else
                    {
                        m_logger.warn("Empy auditing listener from configuration data.");
                    }

                    m_logger.debug("End register auditing listener ["
                                   + listener + "] channel=[" +  channel + "]" );
                    return true;
                }
            }
            else
            {
                m_logger.debug("Begin register listener ["
                               + listener + "] " );
                Map items = Collections.synchronizedMap( new TreeMap() );
                m_listeners.put( listener, items);

                if ( !ListeningUtils.isAuditingListener( listener ) )
                {
                    PluginListenerBean plb = BeansHelper.pluginListenerFromXml( null, xml );
                    if ( plb != null )
                    {
                        registerPlugin( plb );
                        m_logger.debug("register plugin listener successfully.");
                    }
                    else
                    {
                        m_logger.warn("Empy plugin listener from configuration data.");
                    }
                    m_logger.debug("End register listener ["
                                   + listener + "] " );
                    return true;
                }
                else
                {
                    m_logger.debug("Auditing listener always exist, can't register." );
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Register a plugin listener from plugin listener bean.
     * @param plb = Plugin listener bean which contains all listener data
     */
    public void registerPlugin( PluginListenerBean plb )
    {
        m_pluginServiceBean.AddPlugin( plb );

        PluginChannelBean pcb;
        PluginMessageReceiver pmr;

        Map channels = plb.getChannels();

        for ( Iterator lt = channels.values().iterator(); lt.hasNext(); )
        {
            pcb = (PluginChannelBean) lt.next();
            pmr = new PluginMessageReceiver( pcb, plb );
            registerListener( pmr );
        }
    }

    /**
     * Register a plugin instance
     * @param pmr - the plugin instance
     */
    public void registerListener( ListeningThread pmr )
    {
        m_logger.debug("Begin register/start listener thread. " );
        if ( pmr != null )
        {
            String listener = pmr.getListenerName();
            Map items = (Map) m_listeners.get( listener );
            if ( items == null )
            {
                items = Collections.synchronizedMap( new TreeMap() );
                m_listeners.put( listener, items);
            }
            String cid = pmr.getChannelName();
            if ( cid == null || items.containsKey( cid ) )
            {
                m_logger.debug("In [registerPlugin] listener [" + listener
                                   + "] channel [ " + cid
                                   + "] is already exists.");
                return;
            }

            items.put( cid, pmr );
            pmr.register();
        }
        m_logger.debug("End register/start listener thread. " );
    }

    /**
     * Unregister a listener channel or plugin
     * @param listener - the plugin listener
     * @param channel - the channel under the given listener
     * @return - successfully action or not
     */
    public boolean unregisterListener(String listener, String channel)
    {
        boolean flag = false;

        m_logger.debug("Begin unregister/stop listener thread [" + listener + "] [" + channel + "]" );
        if (  listener != null )
        {
            Map items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                m_logger.debug(" Listener found with item's size " + items.size() );

                if ( channel != null )
                {
                    if ( !ListeningUtils.isAuditingListener( listener ) )
                    {
                        PluginListenerBean bean = m_pluginServiceBean.getPluginListener( listener );
                        bean.removeChannel( channel );
                    }
                    else
                    {
                        AuditingManager.getInstance().removeAuditingLogger( channel );
                    }

                    ListeningThread lthread = (ListeningThread) items.get(channel);
                    if ( lthread != null )
                    {
                        lthread.unregister();
                        items.remove( channel );
                        if ( items.size() == 0 )
                        {
                            m_listeners.remove( listener );
                            flag = true;
                        }
                    }
                }
                else {
                    for (Iterator lt = items.values().iterator(); lt.hasNext(); )
                    {
                        ListeningThread lthread = (ListeningThread) lt.next();
                        if ( lthread != null )
                            lthread.unregister();
                    }

                    if ( !ListeningUtils.isAuditingListener( listener ) )
                    {
                        m_listeners.remove( listener );
                        m_pluginServiceBean.removePlugin( listener );
                    }
                    flag = true;
                }
            }
        }
        m_logger.debug("End register/start listener thread. " );
        return flag;
    }

    /**
     * Get the a channel's start/stop status
     * @param listenr - the plugin listener
     * @param channel - the channel under the given listener
     * @return - the defined plugins status: start/stop
     */
    public String getListenerStatus( String listener, String channel )
    {
        m_logger.debug("Begin get Status of listener thread. " );
        if (  listener != null )
        {
            Map items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                if ( channel != null )
                {
                    ListeningThread lthread = (ListeningThread) items.get(channel);
                    if ( lthread != null )
                    {
                        String rstr = lthread.getStatus();
                        m_logger.debug("End get Status of listener thread with status=[ "
                                      + rstr + "]" );
                        return rstr;
                    }
                }
            }
        }
        m_logger.debug("End get Status of listener thread with not found. " );

        return null;
    }

    /**
     * Start listening service which means all suspend channels are resumed
     */
    public void startService()
    {
        String listener;
        Map items;
        ListeningThread lthread;

        for ( Iterator lt = m_listeners.keySet().iterator(); lt.hasNext(); )
        {
            listener = (String)lt.next();

            items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                for (Iterator slt = items.values().iterator(); slt.hasNext(); )
                {
                    lthread = (ListeningThread) slt.next();
                    if ( lthread != null )
                        lthread.start();
                }
            }
        }
    }

    /**
     * Stop listening service which means all channels are suspended
     */
    public void stopService()
    {
        String listener;
        Map items;
        ListeningThread lthread;

        for ( Iterator lt = m_listeners.keySet().iterator(); lt.hasNext(); )
        {
            listener = (String)lt.next();

            items = (Map) m_listeners.get( listener );
            if ( items != null && items.size() > 0 )
            {
                for (Iterator slt = items.values().iterator(); slt.hasNext(); )
                {
                    lthread = (ListeningThread) slt.next();
                    if ( lthread != null )
                        lthread.stop();
                }
            }
        }
    }

    /**
     * Remove plugins that not include in the new list
     * @param plugins - new plugin list
     */
    public void removePlugins( ArrayList plugins )
    {
        String listener;
        Map items;
        ListeningThread lthread;
        ArrayList al = new ArrayList();
        for ( Iterator lt = m_listeners.keySet().iterator(); lt.hasNext(); )
        {
            listener = (String)lt.next();

            if ( !ListeningUtils.isAuditingListener( listener ) && !plugins.contains(listener) )
            {
                al.add(listener);
                items = (Map) m_listeners.get( listener );
                if ( items != null && items.size() > 0 )
                {
                    for (Iterator slt = items.values().iterator(); slt.hasNext(); )
                    {
                        lthread = (ListeningThread) slt.next();
                        if ( lthread != null )
                            lthread.unregister();
                    }
                }
            }
        }

        for (Iterator lt = al.iterator(); lt.hasNext(); )
            m_listeners.remove( lt.next() );
    }

    /**
     * Remove channels that not included in the new channel list
     * @param listener - the listener which channels to be handled
     * @param newChannels - new channel list
     * @return - successfully action or not
     */
    public boolean removeChannels( String listener, ArrayList newChannels )
    {
        String key;
        ListeningThread lthread;
        ArrayList al = new ArrayList();
        Map channels = (Map) m_listeners.get( listener );

        boolean flag = false;

        for ( Iterator lt = channels.keySet().iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();

            if ( !newChannels.contains(key) )
            {
                al.add(  key  );
            }
        }

        for (Iterator lt = al.iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();

            lthread = (ListeningThread) channels.get( key );
            lthread.unregister();
            channels.remove( key );
            if ( channels.size() == 0 )
                m_listeners.remove( listener );
            flag = true;
        }

        return flag;
    }

    /**
     * The plugin channel's attributes are modified
     * @param pmr - the plugin message receiver
     * @param prb - the new plugin channel bean
     * @param plb - the new plugin listener bean
     * @return - successfully action or not
     */
    private boolean resetPluginChannel( PluginMessageReceiver pmr,
                                    PluginChannelBean prb, PluginListenerBean plb )
    {
        return pmr.reset( prb, plb );
    }

    /**
     * Reset attributies of plugin listener
     * @param key - listener identify
     * @param bean - plugin Listener bean
     * @return - successfully action or not
     */
    private boolean resetPluginListener( String key, PluginListenerBean bean )
    {
        Map oldChannels = (Map) m_listeners.get( key );
        ArrayList rchannels = new ArrayList();
        String ckey;
        Map channels = bean.getChannels();
        PluginChannelBean pcb;
        PluginMessageReceiver pmr;

        m_logger.debug("Enter resetPluginListener");
        boolean flag = false;

        for ( Iterator slt = channels.keySet().iterator(); slt.hasNext(); )
        {
            ckey = (String) slt.next();
            pcb = (PluginChannelBean) channels.get( ckey );
            rchannels.add( ckey );
            if ( oldChannels != null && oldChannels.containsKey( ckey ) )
            {
                pmr = (PluginMessageReceiver) oldChannels.get( ckey );
                if ( resetPluginChannel( pmr, pcb, bean ) )
                    flag = true;

                m_logger.debug("reset PluginChannel [" + ckey + "]" );
            }
            else
            {
                pmr = new PluginMessageReceiver( pcb, bean );
                registerListener( pmr );
                flag = true;
            }
        }

        if ( removeChannels( key, rchannels ) )
            flag = true;

        m_logger.debug("Exit resetPluginListener");
        return flag;

    }

    /**
     * Reset all plugin listeners which is usually called when configuration are changed
     * @param bean - plugin service bean
     */
    public void resetPluginService( PluginServiceBean bean )
    {
        ArrayList rplugins = new ArrayList();
        m_pluginServiceBean = bean;

        Map listeners = bean.getPlugins();
        String key, ckey;
        PluginListenerBean plb;
        PluginChannelBean pcb;
        PluginMessageReceiver pmr;
        Map channels, oldChannels;

        m_logger.debug("Enter resetPluginService");

        for ( Iterator lt = listeners.keySet().iterator(); lt.hasNext(); )
        {
            key = (String) lt.next();
            rplugins.add( key );
            plb = (PluginListenerBean) listeners.get( key );

            channels = plb.getChannels();

            if ( m_listeners.containsKey( key ) )
            {
                resetPluginListener(key, plb);
                m_logger.debug("reset plugin [" + key + "]");
            }
            else
            {
                for ( Iterator slt = channels.keySet().iterator(); slt.hasNext(); )
                {
                    ckey = (String) slt.next();
                    pcb = (PluginChannelBean) channels.get( ckey );
                    pmr = new PluginMessageReceiver( pcb, plb );
                    registerListener( pmr );
                }

            }

        }

        removePlugins( rplugins );

        m_logger.debug("Exit resetPluginService");
    }

    /**
     * Handle auditing listener attributes modifications
     * @param amr - auditing listener receiver
     * @param alb - auditing listener bean
     */
    private void resetAuditingChannel( AuditingMessageReceiver amr,
                                      AuditingListenerBean alb )
    {
        amr.reset( alb );
    }

    /**
     * Reset all auditing listeners which is usually called when reload configuration
     * @param listeners - the new listeners list
     */
    public void resetAuditingListeners( Map listeners )
    {
        m_logger.debug("Enter resetAuditingListeners");
        String key;
        Map items = (Map)m_listeners.get( ListeningUtils.AUDITING_LISTENER );
        if ( items == null )
        {
            items = Collections.synchronizedMap( new TreeMap() );
            m_listeners.put( ListeningUtils.AUDITING_LISTENER , items);
        }

        ArrayList al = new ArrayList();
        AuditingMessageReceiver amr;
        AuditingListenerBean alb;

        for ( Iterator lt = listeners.keySet().iterator(); lt.hasNext(); )
        {
            key = (String) lt.next();
            al.add( key );
            alb = (AuditingListenerBean) listeners.get( key );
            if ( items.containsKey( key ) )
            {
                amr = (AuditingMessageReceiver) items.get( key );
                resetAuditingChannel(amr, alb);
                m_logger.debug("reset auditing logger [" + key + "]" );
            }
            else
            {
                amr = new AuditingMessageReceiver( alb );
                registerListener( amr );
            }
        }

        removeChannels( ListeningUtils.AUDITING_LISTENER, al );

        m_logger.debug("Exit resetAuditingListeners");

    }

    /**
     *
     * @return - the registered plugin service bean
     */
    public PluginServiceBean getPluginService()
    {
        return m_pluginServiceBean;
    }


    /**
     * Notify all registered timer service listener
     */
    public void notifyTimerService()
    {
        Object obj;
        TimerServiceListener listener;
        ListeningThread lthread;
        String lstr, cstr;
        Map items;

        if ( m_listeners == null || m_listeners.size() < 1 )
            return;

        m_logger.debug("Enter notifyTimerService.");
        for ( Iterator lt = m_listeners.keySet().iterator(); lt.hasNext(); )
        {
            lstr = (String)lt.next();
            items = (Map)m_listeners.get( lstr );
            if ( items == null )
                continue;

            for ( Iterator slt = items.keySet().iterator(); slt.hasNext(); )
            {
                cstr = (String) slt.next();
                lthread = (ListeningThread) items.get( cstr );
                obj = lthread.m_plugin;
                if ( obj instanceof TimerServiceListener )
                {
                    listener = (TimerServiceListener) obj;
                    listener.timerNotify();
                    m_logger.debug("Notify: [" + lstr + "] [" + cstr + "]" );
                }
            }
        }
        m_logger.debug("Exit notifyTimerService.");
    }

    /**
     * The new listener is defined in the XML string
     * @param xml - XML definition for the listener
     * @return - successfully action or not
     */
    public boolean updatePluginListener( String xml )
    {
        m_logger.debug("Enter updatePluginListener.");
        PluginListenerBean bean = BeansHelper.pluginListenerFromXml( null, xml );
        if ( bean != null )
        {
            String listener = bean.getId();
            if ( !m_listeners.containsKey( listener ) )
            {
                m_logger.debug("PluginListener [" + listener + "] not found, can't update it.");
                return false;
            }

            boolean flag = resetPluginListener( listener, bean );

            m_logger.debug("Exit updatePluginListener with flag=[" + flag + "]");

            return flag;
        }
        else
        {
            m_logger.warn("Can't get plugin listener from configuration data.");
            return false;
        }

    }

    /**
     * The new channel is defined in the XML string
     * @param listener - plug in listener identify
     * @param xml - XML definition for the channel
     * @return - successfully action or not
     */
    public boolean updatePluginChannel( String listener, String xml )
    {
        m_logger.debug("Enter updatePluginChannel.");
        if ( !m_listeners.containsKey( listener ) )
        {
            m_logger.debug("PluginListener [" + listener + "] not found, can't update it's channel.");
            return false;
        }

        PluginChannelBean bean = BeansHelper.pluginChannelFromXml( null, xml );
        if ( bean != null )
        {

            Map lmap = (Map)m_listeners.get( listener );
            PluginMessageReceiver pmr = (PluginMessageReceiver) lmap.get( bean.getId() );

            if ( pmr == null )
            {
                m_logger.debug("PluginChannel [" +  bean.getId() + "] not found, can't update it.");
                return false;
            }

            PluginListenerBean parent = (PluginListenerBean)
                                        m_pluginServiceBean.getPlugins().get( listener );

            return resetPluginChannel( pmr, bean, parent );
        }
        else
        {
            m_logger.warn("Can't set up plugin channel from configuration data.");
            return false;
        }
    }

    /**
     * Update exist auditing listener which properties from the new listener bean
     * @param bean - new listener bean
     */
    public void updateAuditingListener( AuditingListenerBean bean )
    {
        m_logger.debug("Enter updateAuditingListener.");
        if ( !m_listeners.containsKey( ListeningUtils.AUDITING_LISTENER ) )
        {
            m_logger.debug("AuditingListener  not registered yet, can't update it's channel.");
            return;
        }

        Map lmap = (Map)m_listeners.get( ListeningUtils.AUDITING_LISTENER );
        AuditingMessageReceiver amr = (AuditingMessageReceiver) lmap.get( bean.getId() );

        if ( amr == null )
        {
            m_logger.debug("Auditing logger [" +  bean.getId() + "] not found, can't update it.");
            return;
        }

        resetAuditingChannel( amr, bean );

    }

    /**
     * Add a new auditing listener configured from auditing listener bean
     * @param bean - new auditing listener bean
     */
    public void addAuditingListener( AuditingListenerBean bean )
    {
        AuditingMessageReceiver amr = new AuditingMessageReceiver( bean );
        registerListener( amr );
    }
}