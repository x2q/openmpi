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

package com.oncecorp.visa3d.mpi.controller;

import com.oncecorp.visa3d.mpi.intf.payment.MPIErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.DomToMsgConverter;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Description: Authenticator implementation to authenticate incoming Visa 3-D verification / 
 * authentication requests.
 * 
 * @version $Revision: 22 $
 * @author	Alan Zhang
 */

public class AuthenticatorImpl {
	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(AuthenticatorImpl.class.getName());

	/**
	 * Authenticate requests
	 * @param req Request message XML string sent by Merchant Application
	 * @return Response message XML string
	 */
	public static String authenticate(String req) {
		//Debug info
		logger.debug("Authenticating request: " + req);
		//		logger.debug(
		//			"Memory status: Total: "
		//				+ Runtime.getRuntime().totalMemory()
		//				+ ", Free: "
		//				+ Runtime.getRuntime().freeMemory());
		//		logger.debug("Total threads: " + Thread.activeCount());

		Document reqDom = null;
		Message msg = null;
		Message resMsg = null;
		try {
			reqDom = XMLUtil.createDocument(req, false);

			if (reqDom == null) {
				// Create and initialize MPIError message and send it back.
				MPIErrorMessage error =
					(MPIErrorMessage) MessageGenerator.create(
						MPIErrorMessage.MSG_TYPE,
						MPIErrorMessage.MSG_VERSION);
				logger.error(AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

				error.setId("UNKNOWN");
				error.setMerchantID("UNKNOWN");
				error.setErrorCode(AuthenticatorCodes.INVALID_REQUEST_MESSAGE);
				error.setErrorMessage("");
				error.setErrorDetail("[MPI_Interface]");
				error.setVendorCode(
					AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

				error = (MPIErrorMessage) MessageEngine.process(error);

				// Return externalize MPIError message
				//return XMLUtil.toXmlString(error.toXML());
				return error.toString();
			}

			// Set the proper integration layer used by the merchant application
			// This is needed to modofy functionality further down the pipe
			AuthenticatorSession.instance().setPaymentInterface(AuthenticatorSession.XML_INTERFACE);
			
			try {
				// Convert DOM to message
				msg = (new DomToMsgConverter()).convert(reqDom);

				// Check execution status
				ExecutionControl ec = ExecutionControl.getInstance();

				String status = ec.getExecutionStatus();
				if (status.equals(ExecutionControl.STATUS_STOPPED_BY_CONSOLE)
					|| status.equals(ExecutionControl.STATUS_STOPPED_BY_CORE)) {
					MPIErrorMessage error =
						new MPIErrorMessage(
							msg.getId(),
							AuthenticatorCodes.SERVER_STOPPED,
							AuthenticatorCodes.SERVER_STOPPED_MSG,
							"Server stopped at "
								+ ec.getStopTime()
								+ ", Reason: "
								+ ec.getStopReason()
								+ ", Server status: "
								+ status,
							AuthenticatorCodes.SERVER_STOPPED_MSG);
					error.setMerchantID("UNKNOWN");
					error = (MPIErrorMessage) MessageEngine.process(error);

					// Return externalize MPIError message
					// return XMLUtil.toXmlString(error.toXML());
					return error.toString();
				}

				// Process message
				resMsg = MessageEngine.process(msg);

				//Convert response DOM back to String
				// return XMLUtil.toXmlString(resMsg.toXML());
				return resMsg.toString();
			} catch (MessagingException mexcep) {
				// Create and initialize MPIError message and send it back.
				MPIErrorMessage error =
					(MPIErrorMessage) MessageGenerator.create(
						MPIErrorMessage.MSG_TYPE,
						MPIErrorMessage.MSG_VERSION);

				// Create and initialize MPIError message from a MessagingException and send it back.
				logger.error(
					"A message processing error occurred!!! Message=",
					mexcep);
				error.setAll(mexcep);
				String merchantID = AuthenticatorSession.instance().getMerchantID();
				error.setMerchantID(
					(merchantID == null) ? "UNKNOWN" : merchantID);
				error = (MPIErrorMessage) MessageEngine.process(error);

				// Return externalize MPIError message
				// return XMLUtil.toXmlString(error.toXML());
				return error.toString();
			}
		} catch (Exception e) {
			//invalid XML String sent by Merchant App
			logger.error(AuthenticatorCodes.UNEXPECTED_ERROR_MSG, e);

			// Create and initialize MPIError message and send it back.
			// Need to do it DIRECTLY here because of possible endless exception 
			// propagation .....
			MPIErrorMessage error =
				new MPIErrorMessage(
					"UNKNOWN",
					AuthenticatorCodes.UNEXPECTED_ERROR,
					"Unexpected error",
					"[MPI_Interface]",
					AuthenticatorCodes.UNEXPECTED_ERROR_MSG);
			String merchantID = AuthenticatorSession.instance().getMerchantID();
			try {
				error.setMerchantID(
					(merchantID == null) ? "UNKNOWN" : merchantID);
				error = (MPIErrorMessage) MessageEngine.process(error);
			} catch (Exception ee) {
				logger.error(
					"Failed to process MPIErrorMessage before reponse to client.",
					ee);
			}

			// Return externalize MPIError message
			// return XMLUtil.toXmlString(error.toXML());
			return error.toString();
		} finally {
			reqDom = null;
			msg = null;
			resMsg = null;

			// reset ThreadLocal (merchant ID)
			AuthenticatorSession.instance().reset();

		}
	}
}
