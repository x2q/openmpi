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

import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.domain.payment.CRReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.CRResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.publishing.PublishException;
import com.oncecorp.visa3d.mpi.publishing.PublishingManager;

import org.apache.log4j.Logger;

/**
 * MessageEngine is a key member of Messaging Component.
 * As a facade object, it receive the generic message from other MPI
 * component and return a processed message back.
 * 
 * There is 1 public methods can be used by other MPI component:
 * 
 * - public static Message process (Message msg)
 * 
 * On the other hand, MessageEngine is responsible for catching all messaging
 * component exceptions during processing and return an MPIErrorMessage
 * returning to caller (e.g. Authenticator)    
 * 
 * @version $Revision: 36 $
 * @author Jun Shi
 * @author Martin Dufort (mdufort@oncecorp.com)
 */

public class MessageEngine {

	// define internal messaging exception code
	public static final String MESSAGING_EXCEPTION_CODE = "888";
	public static final String UNEXPECTED_EXCEPTION_CODE = "889";

	/**
	 * Generic error message for MPIError message
	 */
	public static final String GENERIC_ERROR_MESSAGE =
		"Unexpected error occurred in Messaging Component.";

	// Local Log4J logger
	private static Logger logger =
		MPILogger.getLogger(MessageEngine.class.getName());

	/** 
	 * Loading all messaging mapping info from MessagingConfigAccessor
	 * and assign to each individual repository defined above
	 */
	private MessageEngine() {
	}

	/**
	 * Process a specific message accoding to it's functional behavior. The process message
	 * takes a message object and applies functional behavior to it be located it's corresponding
	 * processor and invoking it appropriately.
	 * @param msg - Message 
	 * @return msg - Message (processed finalMsg) 
	 */
	public static Message process(Message msg) throws MessagingException {
		MessageProcessor processor = null;

		// Check if message is valid. If not then a MessagingException will be thrown.
		if (msg.validate()) {
			logger.debug("Message is valid: " + msg.getClass().getName());
		}

		// get desired processor object referrence
		MessageUID uid = new MessageUID(msg.getType(), msg.getVersion());

		// instantiate a specific processor class
		processor = (MessageProcessor) MessageMetaInfo.getProcessor(uid);

		// processing this message 
		Message newMsg = processor.process(msg);

		if (!(msg instanceof CRReqMessage) && !(msg instanceof CRResMessage)) {
			// count & publish this message
			PerformanceMonitor.getInstance().count(msg, AuthenticatorSession.instance().getMerchantID());
			
			try {
				PublishingManager.getInstance().publish(msg);
			} catch (PublishException e) {
				logger.error("Current transaction abandoned as publishing error occurred.", e);
				throw new MessagingException(
					msg.getId(),
					ErrorCodes.ERROR_CODE_99,
					ErrorCodes.ERROR_MESSAGE_99,
					"JMS Publishing",
					"Current transaction abandoned as publishing error occurred.",
					"Current transaction abandoned as publishing error occurred.");
			}
		}

		// keep processing
		if (processor instanceof ComplexMessageProcessor) {
			// This guy is a complex processor so we must do additional processing
			ComplexMessageProcessor cProcessor =
				(ComplexMessageProcessor) processor;
			newMsg = cProcessor.processComplex(newMsg);
		}

		// do logging
		logger.debug("process(" + msg.getClass().getName() + ") completed !!");

		// return to caller
		return newMsg;
	}
}
