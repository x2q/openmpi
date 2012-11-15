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

import javax.servlet.http.HttpServletRequest;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.intf.payment.MPIErrorMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLAuthReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLVerifReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLVerifResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMpiErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.intf.payment.html.HTMLFieldDefinition;

/**
 * Description: Authenticator implementation to authenticate incoming Visa 3-D verification /
 * authentication requests.
 *
 * @version $Revision: 7 $
 * @author	Alan Zhang
 */

public class HTMLAuthenticatorImpl implements HTMLFieldDefinition {
	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(HTMLAuthenticatorImpl.class.getName());

	/**
	 * Execution control
	 */
	//private static ExecutionControl ec;

	/**
	 * Authenticate requests
	 * @param req The Http Request
	 * @return Response message HTML string
	 */
	public static String authenticate(HttpServletRequest req) {
		logger.debug("Authenticating request: " + req);

		try {
			// checks execution status
			ExecutionControl ec = ExecutionControl.getInstance();

			String status = ec.getExecutionStatus();
			if (status.equals(ExecutionControl.STATUS_STOPPED_BY_CONSOLE)
				|| status.equals(ExecutionControl.STATUS_STOPPED_BY_CORE))
			{
				MPIErrorMessage error =
						new MPIErrorMessage(
						"UNKNOWN",
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

				HTMLMpiErrorMessage emsg = new HTMLMpiErrorMessage( error );

				return (String) emsg.transform();
			}

			// Set the proper integration layer used by the merchant application
			// This is needed to modofy functionality further down the pipe
			AuthenticatorSession.instance().setPaymentInterface(AuthenticatorSession.HTML_INTERFACE);

			try {
				// checks request type
				if (isEnrollVerifyReq(req)) {
					// enrollment verification request
					// create HTML message
					HTMLVerifReqMessage hvreq = new HTMLVerifReqMessage();

					// populate message with given request's attributes
					hvreq.populate(req);

					// checks message validity
					if (!hvreq.isValid()) {
						// Create and initialize MPIError message and send it back.
						MPIErrorMessage error =
								(MPIErrorMessage) MessageGenerator.create(
								MPIErrorMessage.MSG_TYPE,
		                MPIErrorMessage.MSG_VERSION);
						logger.error(AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

						error.setId("UNKNOWN");
						error.setMerchantID("UNKNOWN");
						error.setErrorCode(AuthenticatorCodes.INVALID_REQUEST_MESSAGE);
						error.setErrorMessage(AuthenticatorCodes.INVALID_HTML_FIELDS_MSG);
						error.setErrorDetail(AuthenticatorCodes.INVALID_HTML_FIELDS);
						error.setVendorCode(
								AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

						error = (MPIErrorMessage) MessageEngine.process(error);

						HTMLMpiErrorMessage emsg = new HTMLMpiErrorMessage( error );

						return (String) emsg.transform();
					}

					// transforms HTMLMessage to Intf message
					PaymentVerifReqMessage pvreq = (PaymentVerifReqMessage) hvreq.transform(  );

					// processes Intf message
					Message result = MessageEngine.process(pvreq);

					// sanity check
					if (result instanceof PaymentVerifResMessage) {
						// prepares HTMLMessage
						HTMLVerifResMessage hvres = new HTMLVerifResMessage();

						// populates HTMLMessage with result message
						hvres.populate(result);

						// transforms HTMLMessage to HTML content
						return (String) hvres.transform(   );
					} else {
						// it must be MPIError message
						// we might create a HTMLErrorMessage to map MPIError message, for now
						// just return error code & detail
						HTMLMpiErrorMessage mpiError = new HTMLMpiErrorMessage( result );
						return (String)mpiError.transform();
					}
				} else {
					// authentication request
					// create HTML message
					HTMLAuthReqMessage hareq = new HTMLAuthReqMessage();

					// populate message with given request's attributes
					hareq.populate(req);

					// checks message validity
					if (!hareq.isValid()) {
						// Create and initialize MPIError message and send it back.
						MPIErrorMessage error =
								(MPIErrorMessage) MessageGenerator.create(
								MPIErrorMessage.MSG_TYPE,
		                        MPIErrorMessage.MSG_VERSION);
						logger.error(AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

						error.setId("UNKNOWN");
						error.setMerchantID("UNKNOWN");
						error.setErrorCode(AuthenticatorCodes.INVALID_REQUEST_MESSAGE);
						error.setErrorMessage(AuthenticatorCodes.INVALID_HTML_FIELDS_MSG);
						error.setErrorDetail(AuthenticatorCodes.INVALID_HTML_FIELDS);
						error.setVendorCode(
								AuthenticatorCodes.INVALID_REQUEST_MESSAGE_MSG);

						error = (MPIErrorMessage) MessageEngine.process(error);

						HTMLMpiErrorMessage emsg = new HTMLMpiErrorMessage( error );

						return (String) emsg.transform();
					}

					// transforms HTMLMessage to Intf message
					PaymentAuthReqMessage authReq = (PaymentAuthReqMessage) hareq.transform(  );

					// processes Intf message
					Message result = MessageEngine.process(authReq);

					// sanity check
					if (result instanceof PaymentAuthResMessage) {
						// prepares HTMLMessage
						HTMLAuthResMessage hares = new HTMLAuthResMessage();

						// populates HTMLMessage with result message
						hares.populate(result);

						// transforms HTMLMessage to HTML content
						return (String) hares.transform(  );
					} else {
						// it must be MPIError message
						// we might create a HTMLErrorMessage to map MPIError message, for now
						// just return error code & detail
						HTMLMpiErrorMessage mpiError = new HTMLMpiErrorMessage( result );
						return (String)mpiError.transform();
					}
				}

			} catch (MessagingException mexcep) {
				logger.error("MessagingException occurred: ", mexcep);
				// Create and initialize MPIError message and send it back.
				MPIErrorMessage error =
						(MPIErrorMessage) MessageGenerator.create(
						MPIErrorMessage.MSG_TYPE,
	                    MPIErrorMessage.MSG_VERSION);

				// Create and initialize MPIError message from a MessagingException and send it back.
				error.setAll(mexcep);
				String merchantID = AuthenticatorSession.instance().getMerchantID();
				error.setMerchantID(
						(merchantID == null) ? "UNKNOWN" : merchantID);
				error = (MPIErrorMessage) MessageEngine.process(error);

				// Return externalize MPIError message
				// return XMLUtil.toXmlString(error.toXML());
				HTMLMpiErrorMessage emsg = new HTMLMpiErrorMessage( error );

				return (String) emsg.transform();
			}
		} catch (Exception e) {
			logger.error("Unexpected error occurred: ", e);

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
			HTMLMpiErrorMessage emsg = new HTMLMpiErrorMessage( error );

			return (String) emsg.transform();
		} finally {
			// reset ThreadLocal
			AuthenticatorSession.instance().reset();
		}
	}

	private static boolean isEnrollVerifyReq(HttpServletRequest req) {
		// need to figure out which attributs used to determine request type
        if ( req.getParameter( TDS_ID ) != null
             && req.getParameter( TDS_MERCHANT_ID ) != null
             && req.getParameter( TDS_CARD_EXPIRY ) != null
             && req.getParameter( TDS_CARD_NUMBER ) != null
             && req.getParameter( TDS_DISPLAY_AMOUNT ) != null
             && req.getParameter( TDS_PURCHASE_AMOUNT ) != null )
             return true;

		return false;
	}

}
