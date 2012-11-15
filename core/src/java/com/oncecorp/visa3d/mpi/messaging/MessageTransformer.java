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
 
package com.oncecorp.visa3d.mpi.messaging;

/**
 *
 * The MessageTransformer interface enables transformation between two message objects.
 * Those transformation are managed by the MessageEngine which holds the correct MessageTransformer
 * class to go from one message type to the destination one.
 *
 * If a transformation scheme is not available, then it should be defined in the messaging properties
 * file along with the proper implementation.
 *
 * @author 	Martin Dufort [mdufort@oncecorp.com]
 * @version 0.1
 */
public interface MessageTransformer {
	/**
	 * Transform an input message into an output message by copying/setting the proper
	 * attributes
	 * 
	 * @param fromMessage	Message to used as transformation input
	 * @param toMessage		Message that needs attributes populated
	 * @return				Resulting message with attributes properly populated
	 * @throws				If any validation did not passed correctly while doing the transformation
	 * @see					MessagingConfigAccessor
	 */
  Message transform(Message fromMessage, Message toMessage) throws MessagingException;
}
