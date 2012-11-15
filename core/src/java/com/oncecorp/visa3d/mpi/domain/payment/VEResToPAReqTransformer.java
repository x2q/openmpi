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

package com.oncecorp.visa3d.mpi.domain.payment;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageTransformer;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

/**
 * Description: Implement transformer class for transformation:
 * VERes Message to PAReq Message when VERes Message field: 
 * enrolled = "Y"
 * 
 * @version 0.1 July 24, 2002
 * @author	Jun Shi
 */

public class VEResToPAReqTransformer implements MessageTransformer {

    // VEResToPAReqTransformer type
	public final static String TRANSFORMER_TYPE = "VEResToPAReq";

    // Local Log4J logger
	public Logger logger = MPILogger.getLogger(
	                       VEResToPAReqTransformer.class.getName());

	/**
	 * Implement MessageTransformer interface method: transform(fromMsg, toMsg)
	 *
	 * @param VEResMessage and an empty PAReqMessage
	 * @return a partly populated PAReqMessage
	 */
	public Message transform(Message fromMessage, Message toMessage) 
	    throws MessagingException 
	{
		//Sanity check
		if (!(fromMessage instanceof VEResMessage)
		   || !(toMessage instanceof PAReqMessage)) 
		{
		   this.logger.error("VEResToPAReqTransformer:"
		                    +"Incompatible messsage passed in.");
		   throw new MessagingException("VEResToPAReqTransformer:"
		                               +"Incompatible messsage passed in.");
		}

        // type casting 
        VEResMessage vres_msg = (VEResMessage) fromMessage; 
        PAReqMessage preq_msg = (PAReqMessage) toMessage;

        // set CH acctID
        preq_msg.setChAcctID(vres_msg.getAcctID());               
          
        //return a partly populated PAReqMessage
		return preq_msg;
 	}
}