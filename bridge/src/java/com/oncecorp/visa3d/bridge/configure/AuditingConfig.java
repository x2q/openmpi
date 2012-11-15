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

import org.w3c.dom.Document;
import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.auditing.AuditingManager;
import com.oncecorp.visa3d.bridge.beans.AuditingServiceBean;
import com.oncecorp.visa3d.bridge.listening.ListeningManager;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;


/**
 * <p>Title: Auditing Config </p>
 * <p>Description: Implement Configurable interface, configure the auditing service</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com)
 * @version 1.0
 */

public class AuditingConfig implements Configurable
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            AuditingConfig.class.getName() );

    /**
     * Default Constructor
     */
    public AuditingConfig()
    {
    }

    /**
     * Configure Interface
     * @param root - the new databridge bean
     */
    public void config( DataBridgeBean root )
    {
        m_logger.debug("Enter config");
        AuditingServiceBean bean = root.getAuditingService();
        //AuditingServiceBean oldBean = AuditingManager.getInstance().getBean();
        AuditingManager.getInstance().setBean(bean);
        ListeningManager.getInstance().resetAuditingListeners( bean.getListeners() );
        //ListeningManager.getInstance().resetAuditingListeners(
        //  AuditingManager.getInstance().getBean().getListeners()
        //);
        m_logger.debug("Exit config");
    }

}