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

package com.oncecorp.visa3d.mpi.intf.payment;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * <p>Title: UniqueWindowIDGenerator</p>
 * <p>Description: Generate an unique windows identify and associate it with message id.
 * Why we don't use message id as it includes some special character that cann't be used
 * as windows name.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class UniqueWindowIDGenerator
{
	private static long   m_seqNum = 0;
	private static Map    m_idMapping = Collections.synchronizedMap( new HashMap() );

	/**
	 * Default Constructor
	 */
    public UniqueWindowIDGenerator()
    {
    }

	/**
	 * Generate and cached the windows id
	 * @param msgId - message identify
	 * @return - window's unique id string
	 */
	public synchronized static  String getWindowID( String msgId )
	{
		if ( m_idMapping.containsKey( msgId ) )
	        return (String) m_idMapping.get( msgId );
        else
		{
			String id =  "" + System.currentTimeMillis()  + ( m_seqNum++ );
			m_idMapping.put( msgId, id );
			return id;
		}
	}

}