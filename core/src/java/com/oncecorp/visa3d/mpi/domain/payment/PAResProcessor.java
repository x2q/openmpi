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
import com.oncecorp.visa3d.mpi.messaging.MessageProcessor;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

/**
 * Description: Implement processor class for Message: PARes.
 * The processor stores PARes messaging in database via Persistent
 * Component.
 * 
 * @version 0.1 July 24, 2002
 * @author	Alan Zhang
 */
public class PAResProcessor implements MessageProcessor {
	/**
	 * PAResProcessor type
	 */
	public final static String PROCESSOR_TYPE = "PARes";

	/**
	 * Local Log4J logger
	 */
	public Logger logger = MPILogger.getLogger(PAResProcessor.class.getName());

	/**
	 * Implement MessageProcessor interface method
	 */
	public Message process(Message inMsg) throws MessagingException {
		//Sanity check
		if (!(inMsg instanceof PAResMessage)) {
			this.logger.error(
				"PAResProcessor can only process PAResMessage. Incompatible messsage passed in.");
			throw new MessagingException("Incompatible message type.");
		}

		/*
		 * No longer store message in EJB beans
		//Type casting
		PAResMessage msg = (PAResMessage) inMsg;

		//Now we are ready to save this message to database
		try {
			//Create JNDI initial context
			javax.naming.Context initialContext = new javax.naming.InitialContext();

			Object objref =
				initialContext.lookup(MPIConfigDefinition.PERSISTENT_PARESMSG_JNDI);

			//Narrow down
			PAResMsgHome home =
				(PAResMsgHome) PortableRemoteObject.narrow(objref, PAResMsgHome.class);

			//Create messsage
			home.create(msg);
		} catch (CreateException ce) {
			this.logger.error("Failed to save message.", ce);
			throw new MessagingException("Storing PAReqMessage failed.");
		} catch (NamingException ne) {
			this.logger.error("Name lookup failed.", ne);
			throw new MessagingException("Storing PAReqMessage failed.");
		} catch (RemoteException re) {
			this.logger.error("Failed to save message.", re);
			throw new MessagingException("Storing PAReqMessage failed.");
		}
		*/

		//return message itself
		return inMsg;
	}
}