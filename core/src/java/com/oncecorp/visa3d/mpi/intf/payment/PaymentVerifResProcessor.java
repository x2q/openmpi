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

/**
 * Description: Implement processor class for Message: PaymentVerifRes.
 * The processor validates PaymentVerifRes message fields to make sure all
 * mandatory fields presented and with correct size. A MessagingException 
 * will be thrown in case any invalid field exists.
 * 
 * @version 0.1 July 24, 2002
 * @author	Jun Shi
 */
package com.oncecorp.visa3d.mpi.intf.payment;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageProcessor;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

public class PaymentVerifResProcessor implements MessageProcessor {

    // PaymentVerifResProcessor type
	public final static String PROCESSOR_TYPE = "PaymentVerifRes";

    // Local Log4J logger
	public Logger logger = MPILogger.getLogger(PaymentVerifResProcessor.class.getName());

	/**
	 * Implement MessageProcessor interface method
	 */
	public Message process(Message inMsg) throws MessagingException 
	{
		//Sanity check
		if (!(inMsg instanceof PaymentVerifResMessage)) 
		{
			this.logger.error(
				"PaymentVerifResProcessor can only process PaymentVerifResMessage."
				+"Incompatible messsage passed in.");
			throw new MessagingException("PaymentVerifResProcessor: Incompatible messsage passed in.");
		}

		//return the message itself
		return inMsg;
	}

}