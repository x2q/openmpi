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

package com.oncecorp.visa3d.bridge.configure;

import java.util.Map;

import com.oncecorp.visa3d.bridge.jmxagent.BridgeAgentManager;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.MonitoringServiceBean;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;

/**
 * <p>Title: MonitoringServiceConfig</p>
 * <p>Description: Configure JMX related resources</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class MonitoringServiceConfig implements Configurable
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            MonitoringServiceConfig.class.getName() );

    private static boolean m_isAgentInit = false;

    /**
     * Default constructor
     */
    public MonitoringServiceConfig()
    {
    }

    /**
     * Configure Interface
     * @param root - the new databridge bean
     */
    public void config( DataBridgeBean root )
    {

        m_logger.debug("Enter config");

        MonitoringServiceBean bean = root.getMonitoringService();

        Map props = bean.getProperties();
        if ( !m_isAgentInit )
        {
            try {
                BridgeAgentManager.initAgent( props );
                m_isAgentInit = true;
            } catch ( Exception e )
            {
                m_logger.error( "Initialize JMX agent error.", e);
            }
        }
        m_logger.debug("Exit config");

    }

}