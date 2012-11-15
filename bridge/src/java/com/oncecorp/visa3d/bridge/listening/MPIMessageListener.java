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
import org.w3c.dom.Document;

/**
 * <p>Title: MPIMessageListener</p>
 * <p>Description: The generic message listener interface that all listener should implement.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public interface MPIMessageListener
{
    /**
     * msg is a XML document, which is from core mpi and after message/field filter and
     * field mask operation
     */
     public boolean handleMsg( Properties props, Document msg ) ;

     /**
      * Pass the custom configuration data to the plug-in.
      */
     public void config ( String xml );

     /**
      * @return whether the plug-in has been intialized
      */
     public boolean isInitialized();

     /**
      * start up plugin
      */
     public void start() ;

     /**
      * stop plugin
      */
     public void stop() ;

     /**
      * Register plugin
      */
     public void register();

     /**
      * Unregister plugin
      */
     public void unregister();

     /**
      * Plug in listener name
      */
     public String getName();

     /**
      * Get the description of plugin
      */
     public String getDescription();

     /**
      *
      * @return - whether the plugin will accept filter change
      */
     public boolean acceptFilterChange();

     /**
      *
      * @return - whether the plugin will accept dynamic filter change
      */
     public boolean acceptDynamicFilterChange();

     /**
      * Set the filter to the plugin
      * @param props - contains merchant ids and message type-version list
      */
     public void setFilter( Properties props );

     /**
      *
      * @return - whether the plugin will accept any filter
      */
     public boolean acceptFilter();

}