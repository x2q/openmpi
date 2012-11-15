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

import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.intf.payment.MPIErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageTransformer;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

/**
 * Description: Implement transformer class for transformation:
 * Error Message to MPIError Message
 * 
 * @version 0.1 July 24, 2002
 * @author	Jun Shi
 */

public class ErrorToMPIErrorTransformer implements MessageTransformer {

	// ErrorToMPIErrorTransformer type
	public final static String TRANSFORMER_TYPE = "ErrorToMPIError";

	// Local Log4J logger
	public Logger logger =
		MPILogger.getLogger(ErrorToMPIErrorTransformer.class.getName());

	/**
	 * Implement MessageTransformer interface method: transform(fromMsg, toMsg)
	 *  
	 * @param ErrorMessage and an empty MPIErrorMessage
	 * @return a populated MPIErrorMessage
	 */
	public Message transform(Message fromMessage, Message toMessage)
		throws MessagingException {
		//Sanity check
		if (!(fromMessage instanceof ErrorMessage)
			|| !(toMessage instanceof MPIErrorMessage)) {
			this.logger.error(
				"ErrorToMPIErrorTransformer:"
					+ "Incompatible messsage passed in.");
			throw new MessagingException(
				"ErrorToMPIErrorTransformer:"
					+ "Incompatible messsage passed in.");
		}

		// type casting 
		ErrorMessage errMsg = (ErrorMessage) fromMessage;
		MPIErrorMessage mpiErrMsg = (MPIErrorMessage) toMessage;

		// set message id from original Error message
		mpiErrMsg.setId(errMsg.getId());

		// set errorCode
		mpiErrMsg.setErrorCode(errMsg.getErrorCode());

		// set errorMessage
		mpiErrMsg.setErrorMessage(errMsg.getErrorMessage());

		// set errorDetail
		mpiErrMsg.setErrorDetail(errMsg.getErrorDetail());

		// set errorCode
		mpiErrMsg.setVendorCode(errMsg.getVendorCode());

		String merchantID = AuthenticatorSession.instance().getMerchantID();
		mpiErrMsg.setMerchantID((merchantID == null) ? "UNKNOWN" : merchantID);
		
		//return a populated MPIErrorMessage
		return mpiErrMsg;
	}
}