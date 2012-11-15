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

package com.oncecorp.visa3d.bridge.plugin;

import java.util.Properties;

import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;

import org.w3c.dom.Document;


import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

import com.oncecorp.visa3d.bridge.listening.MPIMessageListener;

/**
 * <p>Title: PluginRoot</p>
 * <p>Description: Define the common interface and behavior of the plugins. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public abstract class PluginRoot implements MPIMessageListener
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            PluginRoot.class.getName() );

    /**
     * Default constructor
     */
    public PluginRoot(  )
    {
        m_logger.debug( "In [PluginRoot Constructor]");

        m_logger.debug( "Out [PluginRoot Constructor]");

    }

    /**
     * msg is a XML document, which is from core mpi and after message/field filter and
     * field mask operation
     */
     public abstract boolean handleMsg( Properties props, Document msg ) throws PluginException;

     /**
      * @return whether the plug-in has been intialized
      */
     public abstract boolean isInitialized();

     /**
      * Pass the custom configuration data to the plug-in.
      */
     public abstract void config ( String xml );

     /**
      * initialize plugin
      */
     public abstract void init() throws PluginException;

     /**
      * start up plugin
      * @throws PluginException
      */
     public abstract void start() throws PluginException;

     /**
      * stop plugin
      * @throws PluginException
      */
     public abstract void stop() throws PluginException;


     /**
      * Plug in listener name
      * @return
      */
     public abstract String getName();

     /**
      * Get the description of plugin
      * @return
      */
     public abstract String getDescription();

     /**
      * Implement message listening interface. In case we use Server session pool
      * or MDB ( Message Driven Bean )
      * @param msg - JMS message
      * @throws PluginException
      */
     public void onMessage(Message msg) throws PluginException
     {
         m_logger.debug("In [PluginRoot.onMessage] enter");
         try {
             if ( msg instanceof TextMessage )
             {
                 m_logger.debug("Get a message:" + ((TextMessage)msg).getText());
             }
         } catch ( Exception e )
         {
             m_logger.error("In onMessage", e);
         }
         handleMsg( null, null );
     }

     /**
      *
      * @return - whether the plugin will accept filter change
      */
     public boolean acceptFilterChange()
     {
         return true;
     }

     /**
      * Unregister plugin
      */
     public void unregister()
     {

     }

     /**
      * Register plugin
      */
     public void register()
     {

     }

     /**
      *
      * @return - whether the plugin will accept dynamic filter change
      */
     public boolean acceptDynamicFilterChange()
     {
         return true;
     }

     /**
      * Set the filter to the plugin
      * @param props - contains merchant ids and message type-version list
      */
     public void setFilter( Properties props )
     {

     }

     /**
      *
      * @return - whether the plugin will accept any filter
      */
     public boolean acceptFilter()
     {
         return true;
     }

}
