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
import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.w3c.dom.Document;
import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import com.oncecorp.visa3d.bridge.auditing.AuditingManager;
import com.oncecorp.visa3d.bridge.utility.Utils;

/**
 * <p>Title: AuditingMessageReceiver </p>
 * <p>Description: Inherit from ListeningThread and call auditing handler</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class AuditingMessageReceiver extends ListeningThread
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            AuditingMessageReceiver.class.getName() );

    private AuditingListenerBean m_bean = null;

    /**
     * Constructor with the channel identify
     * @param channel - channel identify
     */
    public AuditingMessageReceiver( AuditingListenerBean bean )
    {
        m_logger.debug("Enter Constructor AuditingMessageReceiver");

        if ( bean == null )
        {
            m_logger.error("Empty AuditingListenerBean" );
            setStatus( Utils.LISTENING_EXCEPTION );
            return;
        }

        m_bean = bean;
        super.init( ListeningUtils.AUDITING_LISTENER, bean.getId(), bean.getStatus() );

        setMerchantIDs( bean.getMerchantIds() );
        setMessagesMap( bean.getMessages() );

        m_plugin = (MPIMessageListener)AuditingManager.getInstance().getLogger( m_bean.getId() );
        if ( m_plugin == null )
        {
            m_logger.error("Can't find auditing logger [" + m_bean.getId() + "]" );
            setStatus( Utils.LISTENING_EXCEPTION );
        }
        else
            m_logger.debug("get logger [" + m_bean.getId() + "] from AuditingManager" );

        m_logger.debug("Exit Constructor AuditingMessageReceiver");

    }

    /**
     * Check and decide whether the bean is any different with the old one and if
     * it is different, do the reset action.
     * @param bean - auditing listener bean contains new configuration data.
     */
    public void reset( AuditingListenerBean bean )
    {
        m_logger.debug("Enter reset of AuditingMessageReceiver.");
        boolean notChangeFilter = ListeningUtils.merchantListEqual(
                bean.getMerchantIds(), m_merchantIDs ) &&
             ListeningUtils.messageMapEqual( bean.getMessages(),
                m_messages, false );

        String status = m_bean.getStatus();

        m_logger.debug("Stop auditing listener first.");
        super.stop();

        if ( !notChangeFilter )
        {
            setMerchantIDs( bean.getMerchantIds() );
            super.changeFilter();
        }

        setMessagesMap( bean.getMessages() );
        m_bean = bean;
        setStatus( status );
        m_plugin = (MPIMessageListener)AuditingManager.getInstance().getLogger( m_bean.getId() );

        if ( !ListeningUtils.isStopAction( status ) )
            super.start();

        m_logger.debug("Exit reset of AuditingMessageReceiver.");
    }

    /**
     *
     * @param status - Auditing message bean status
     */
    protected void setAuditingListenerBeanStatus( String status )
    {
        m_bean.setStatus( status );
    }
}