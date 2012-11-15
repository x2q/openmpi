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

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.PluginChannelBean;
import com.oncecorp.visa3d.bridge.beans.PluginListenerBean;
import com.oncecorp.visa3d.bridge.utility.Utils;

/**
 * <p>Title: PluginMessgeReceiver</p>
 * <p>Description: Inherit from ListeningThread. Load and calll Plugin functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class PluginMessageReceiver extends ListeningThread
{
    private PluginChannelBean m_bean = null;
    private String m_classname = null;
    private static Map m_loadedClass = Collections.synchronizedMap( new HashMap() );

    private static Logger m_logger = DataBridgeLoger.getLogger(
            PluginMessageReceiver.class.getName() );

    /**
     * Constructor with data from plugin channel bean and listener bean
     * @param bean -  plugin channel bean
     * @param parent - plugin listener bean
     */
    public PluginMessageReceiver( PluginChannelBean bean, PluginListenerBean parent )
    {
        m_logger.debug("Constructor PluginMessageReceiver");

        String classname = parent.getClassName();
        m_classname = classname;
        if ( classname == null || classname.trim().equals("") )
        {
            m_logger.error("Class name is not defined");
            return;
        }

        pluginInit( bean, parent );

        m_logger.debug("Exit Constructor PluginMessageReceiver");

    }

    /**
     * Initialize plugin with data from plugin channel bean and listener bean
     * @param bean -  plugin channel bean
     * @param parent - plugin listener bean
     */
    private void pluginInit( PluginChannelBean bean, PluginListenerBean parent )
    {

        m_bean = bean;
        setMessagesMap( parent.getMessages() );
        setMerchantIDs( bean.getMerchantIds() );
        if ( !initInstance() )
        {
            super.init( parent.getId(), bean.getId(),
                        ListeningUtils.PLUGIN_CLASS_NOT_INIT );
            return;
        }

        super.init( parent.getId(), bean.getId(), bean.getStatus() );

        synchronized ( m_plugin )
        {
            m_plugin.config( bean.getCustomData() );
        }


    }

    /**
     * Load the class and new a plugin instance
     * @return - success or not
     */
    private boolean initInstance()
    {
        try {
            Class cls = (Class)m_loadedClass.get( m_classname );
            if ( cls  == null )
            {
                try {
                    ClassLoader cl = this.getClass().getClassLoader();
                    cls = cl.loadClass( m_classname );
                    m_loadedClass.put( m_classname, cls );
                } catch ( Exception e )
                {
                    m_logger.error("Can't load the plug-in's class:["
                                       + m_classname + "]", e);
                    m_plugin = null;
                    return false;
                }

            }
            m_plugin = (MPIMessageListener)cls.newInstance();
            m_bean.setAcceptDynamicFilter( m_plugin.acceptDynamicFilterChange() );
            m_bean.setAcceptFilter( m_plugin.acceptFilterChange() );
        } catch ( Exception e )
        {
            m_logger.error("Can't new plug-in's instance [" + m_classname + "].", e);
            return false;
        }

        return true;
    }

    /**
     * The plugin channel's attributes are modified
     * @param prb - the new plugin channel bean
     * @param plb - the new plugin listener bean
     * @return - successfully action or not
     */
    public boolean reset( PluginChannelBean bean, PluginListenerBean parent )
    {
        m_logger.debug("Enter reset");

        boolean flag = false;

        boolean filterFlag = false;
        String classname = parent.getClassName();

        if ( classname == null && m_classname != null )
        {
            super.unregister();
            if ( m_plugin != null )
            {
                synchronized ( m_plugin )
                {
                    m_plugin = null;
                }
            }
            m_classname = null;
            m_bean = bean;
            setMessagesMap( parent.getMessages() );
            setMerchantIDs( bean.getMerchantIds() );
            setStatus( bean.getStatus() );
            return true;
        }
        else if ( classname != null && m_classname == null )
        {
            m_classname = classname;
            pluginInit( bean, parent );
            super.register();
            return true;
        }

        boolean notChangeFilter = ListeningUtils.merchantListEqual(
                     bean.getMerchantIds(), m_merchantIDs ) &&
                ListeningUtils.messageMapEqual( parent.getMessages(),
                     m_messages, false );

        boolean cflag = false;
        if ( classname != null && m_classname != null )
        {
            if ( !classname.trim().equals( m_classname.trim() ) )
            {
                m_classname = classname;
                cflag = true;
            }
        }

        String status = bean.getStatus();

        if ( !notChangeFilter && !status.equals( getStatus() ) )
            flag = true;

        m_bean = bean;
        if ( cflag || !notChangeFilter || status.equals( Utils.LISTENING_STOP) )
            super.stop();
        if ( cflag )
            initInstance();
        setMessagesMap( parent.getMessages() );
        setMerchantIDs( bean.getMerchantIds() );
        if ( !notChangeFilter )
            super.changeFilter();

        setStatus( status );

        if ( !ListeningUtils.isStopAction( status ) )
            super.start();

        m_logger.debug("Exit reset");
        return flag;

    }

    /**
     *
     * @param status -  PluginChannelBean status
     */
    protected void setPluginChannelBeanStatus( String status )
    {
        m_bean.setStatus( status );
    }

}