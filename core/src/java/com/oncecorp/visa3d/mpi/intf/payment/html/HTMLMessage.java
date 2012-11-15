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

package com.oncecorp.visa3d.mpi.intf.payment.html;

import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;

/**
 * Abstract class for all SecureCode messages
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public abstract class HTMLMessage {

	public abstract boolean isValid();

	public abstract Object transform() throws MessagingException;

	public abstract void populate(Object o);

 	public static void printHiddenFields( StringBuffer sb, String name, Object value )
	{
		if ( value != null )
		{
			sb.append("          <input type=\"hidden\" name=\"");
			sb.append( name );
			sb.append("\" value=\"");
			sb.append( value );
			sb.append("\">\n");
		}
	}
}
