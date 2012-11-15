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

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: ConnectionExceptionListener </p>
 * <p>Description: If a JMS provider detects a serious problem with a Connection
 *  it will inform this listener and exception status will set up.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class ConnectionExceptionListener implements ExceptionListener
{
    private ListeningThread m_lt = null;

    /**
     * Contructor with ListeningThread instance
     * @param lt - ListeningThread instance
     */
    public ConnectionExceptionListener( ListeningThread lt )
    {
        m_lt = lt;
    }

    /**
     * Notify ListeningThread of a JMS exception.
     * @param exception - JMS Exception
     */
    public void onException(JMSException exception)
    {
        DataBridgeLoger.getLogger( this.getClass() ).error( "Exception in ["
                + m_lt.getListenerName() + "] [" + m_lt.getChannelName() + "]",
                exception );
        if ( m_lt != null )
            m_lt.setExceptionStatus();
    }

}