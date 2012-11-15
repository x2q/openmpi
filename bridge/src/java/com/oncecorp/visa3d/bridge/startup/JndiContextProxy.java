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

package com.oncecorp.visa3d.bridge.startup;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: JndiContextProxy </p>
 * <p>Description: A proxy for Jndi related functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com)
 * @version 1.0
 */
public class JndiContextProxy
{

    protected  static Context m_context = null;

    /**
     * Default Constructor
     */
    protected JndiContextProxy()
    {
    }

    /**
     * Get initial JNDI context
     * @return - JNDI context
     */
    public static Context getContext()
    {
        DataBridgeLoger.getLogger( JndiContextProxy.class ).debug("Enter getContext()");
        try {
            if ( m_context == null )
            {
                if ( StartupProxy.isServerContext() )
                    m_context = new InitialContext();
                else
				{
					try {
						m_context =  new InitialContext(
								StartupProxy.getInitParameters() );
					} catch ( Exception e )
					{
						// try server context again
						m_context = new InitialContext();
					}
				}

            }
        } catch ( Exception e )
        {
            m_context = null;
            DataBridgeLoger.getLogger( JndiContextProxy.class ).error( "Exception in getContext", e );
        }

        DataBridgeLoger.getLogger( JndiContextProxy.class ).debug("Exit getContext()");
        return m_context;
    }



}